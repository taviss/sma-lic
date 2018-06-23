package com.sma.object.finder.tf;

import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import org.tensorflow.*;
import org.tensorflow.types.UInt8;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Tensorflow implementation for the {@link ObjectRecognizer}
 * This implementation was copied from the official Tensorflow repository and slightly modified
 */
public class TensorflowObjectDetectionAPI implements ObjectRecognizer {
    
    // Only return this many results.
    private static final int MAX_RESULTS = 100;

    // Config values.
    private String inputName;
    //private int inputSize;

    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    //private int[] intValues;
    //private byte[] byteValues;
    private float[] outputLocations;
    private float[] outputScores;
    private float[] outputClasses;
    private float[] outputNumDetections;
    private String[] outputNames;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     */
    public static ObjectRecognizer create(
            final String modelFilename,
            final String labelFilename,
            final int inputSize) throws IOException {
        final TensorflowObjectDetectionAPI d = new TensorflowObjectDetectionAPI();

        InputStream labelsInput = new FileInputStream(labelFilename);
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            d.labels.add(line);
        }
        br.close();


        d.inferenceInterface = new TensorFlowInferenceInterface(modelFilename);

        final Graph g = d.inferenceInterface.graph();

        d.inputName = "image_tensor";
        // The inputName node has a shape of [N, H, W, C], where
        // N is the batch size
        // H = W are the height and width
        // C is the number of channels (3 for our purposes - RGB)
        final Operation inputOp = g.operation(d.inputName);
        if (inputOp == null) {
            throw new RuntimeException("Failed to find input Node '" + d.inputName + "'");
        }
        //d.inputSize = inputSize;
        // The outputScoresName node has a shape of [N, NumLocations], where N
        // is the batch size.
        final Operation outputOp1 = g.operation("detection_scores");
        if (outputOp1 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_scores'");
        }
        final Operation outputOp2 = g.operation("detection_boxes");
        if (outputOp2 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_boxes'");
        }
        final Operation outputOp3 = g.operation("detection_classes");
        if (outputOp3 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_classes'");
        }

        // Pre-allocate buffers.
        d.outputNames = new String[] {"detection_boxes", "detection_scores",
                "detection_classes", "num_detections"};
        d.outputScores = new float[MAX_RESULTS];
        d.outputLocations = new float[MAX_RESULTS * 4];
        d.outputClasses = new float[MAX_RESULTS];
        d.outputNumDetections = new float[1];
        return d;
    }

    private TensorflowObjectDetectionAPI() {}

    @Override
    public List<Recognition> identifyImage(byte[] imageBytes) {
        int size = 224;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(in);
            size = Integer.min(bufferedImage.getHeight(), bufferedImage.getWidth());
        } catch(IOException e) {
            // NO-OP;
        }
        return identifyImage(imageBytes, size);
    }

    @Override
    public List<Recognition> identifyImage(byte[] imageBytes, int inputSize) {
        System.out.println("API#identifyImage[size=" + inputSize + "]");

        try {
            Tensor<UInt8> image = constructAndExecuteGraphToNormalizeImage(imageBytes);
            inferenceInterface.addFeed(inputName, image);
        } catch(IOException e) {
            return new ArrayList<>();
        }

        // Run the inference call.
        inferenceInterface.run(outputNames, logStats);

        // Copy the output Tensor back into the output array.
        outputLocations = new float[MAX_RESULTS * 4];
        outputScores = new float[MAX_RESULTS];
        outputClasses = new float[MAX_RESULTS];
        outputNumDetections = new float[1];
        inferenceInterface.fetch(outputNames[0], outputLocations);
        inferenceInterface.fetch(outputNames[1], outputScores);
        inferenceInterface.fetch(outputNames[2], outputClasses);
        inferenceInterface.fetch(outputNames[3], outputNumDetections);

        // Find the best detections.
        final PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        1,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(final Recognition lhs, final Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        // Scale them back to the input size.
        for (int i = 0; i < outputScores.length; ++i) {
            if(outputScores[i] == 0) {
                continue;
            }

            int left = (int)(outputLocations[4 * i + 1] * inputSize);
            int top = (int)(outputLocations[4 * i] * inputSize);
            int right = (int)(outputLocations[4 * i + 3] * inputSize);
            int bottom = (int)(outputLocations[4 * i + 2] * inputSize);

            pq.add(
                    new Recognition("" + i, labels.get((int) outputClasses[i]), outputScores[i], imageBytes,
                            left, top, right, bottom));
        }

        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        for (int i = 0; i < Math.min(pq.size(), MAX_RESULTS); ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    private static void bgr2rgb(byte[] data) {
        for (int i = 0; i < data.length; i += 3) {
            byte tmp = data[i];
            data[i] = data[i + 2];
            data[i + 2] = tmp;
        }
    }

    private static Tensor<UInt8> constructAndExecuteGraphToNormalizeImage(byte[] image) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));
        if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            throw new IOException(
                    String.format(
                            "Expected 3-byte BGR encoding in BufferedImage, found %d (file: %s). This code could be made more robust",
                            img.getType(), "TBA"));
        }
        byte[] data = ((DataBufferByte) img.getData().getDataBuffer()).getData();
        // ImageIO.read seems to produce BGR-encoded images, but the model expects RGB.
        bgr2rgb(data);
        final long BATCH_SIZE = 1;
        final long CHANNELS = 3;
        long[] shape = new long[] {BATCH_SIZE, img.getHeight(), img.getWidth(), CHANNELS};
        return Tensor.create(UInt8.class, shape, ByteBuffer.wrap(data));
    }
}
