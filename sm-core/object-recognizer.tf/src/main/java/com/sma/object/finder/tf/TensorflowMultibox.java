package com.sma.object.finder.tf;

/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import org.tensorflow.*;


/**
 * A detector for general purpose object detection as described in Scalable Object Detection using
 * Deep Neural Networks (https://arxiv.org/abs/1312.2249).
 */
public class TensorflowMultibox implements ObjectRecognizer {
    // Only return this many results.
    private static final int MAX_RESULTS = Integer.MAX_VALUE;

    // Config values.
    private String inputName;
    private int inputSize;
    private int imageMean;
    private float imageStd;

    // Pre-allocated buffers.
    private int[] intValues;
    private float[] floatValues;
    private float[] outputLocations;
    private float[] outputScores;
    private String[] outputNames;
    private int numLocations;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    private float[] boxPriors;

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param locationFilename The filepath of label file for classes.
     * @param inputSize The input size. A square image of inputSize x inputSize is assumed.
     * @param imageMean The assumed mean of the image values.
     * @param imageStd The assumed std of the image values.
     * @param inputName The label of the image input node.
     * @param outputName The label of the output node.
     */
    public static ObjectRecognizer create(
            final String modelFilename,
            final String locationFilename,
            final int imageMean,
            final float imageStd,
            final String inputName,
            final String outputLocationsName,
            final String outputScoresName) {
        final TensorflowMultibox d = new TensorflowMultibox();

        d.inferenceInterface = new TensorFlowInferenceInterface(modelFilename);

        final Graph g = d.inferenceInterface.graph();

        d.inputName = inputName;
        // The inputName node has a shape of [N, H, W, C], where
        // N is the batch size
        // H = W are the height and width
        // C is the number of channels (3 for our purposes - RGB)
        final Operation inputOp = g.operation(inputName);
        if (inputOp == null) {
            throw new RuntimeException("Failed to find input Node '" + inputName + "'");
        }
        d.inputSize = (int) inputOp.output(0).shape().size(1);
        d.imageMean = imageMean;
        d.imageStd = imageStd;
        // The outputScoresName node has a shape of [N, NumLocations], where N
        // is the batch size.
        final Operation outputOp = g.operation(outputScoresName);
        if (outputOp == null) {
            throw new RuntimeException("Failed to find output Node '" + outputScoresName + "'");
        }
        d.numLocations = (int) outputOp.output(0).shape().size(1);

        d.boxPriors = new float[d.numLocations * 8];

        try {
            d.loadCoderOptions(locationFilename, d.boxPriors);
        } catch (final IOException e) {
            throw new RuntimeException("Error initializing box priors from " + locationFilename);
        }

        // Pre-allocate buffers.
        d.outputNames = new String[] {outputLocationsName, outputScoresName};
        d.intValues = new int[d.inputSize * d.inputSize];
        d.floatValues = new float[d.inputSize * d.inputSize * 3];
        d.outputScores = new float[d.numLocations];
        d.outputLocations = new float[d.numLocations * 4];

        return d;
    }

    private TensorflowMultibox() {}

    private void loadCoderOptions(final String locationFilename, final float[] boxPriors)
            throws IOException {
        InputStream is = new FileInputStream(locationFilename);

        // Read values. Number of values per line doesn't matter, as long as they are separated
        // by commas and/or whitespace, and there are exactly numLocations * 8 values total.
        // Values are in the order mean, std for each consecutive corner of each box, for a total of 8
        // per location.
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int priorIndex = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            final StringTokenizer st = new StringTokenizer(line, ", ");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                try {
                    final float number = Float.parseFloat(token);
                    boxPriors[priorIndex++] = number;
                } catch (final NumberFormatException e) {
                    // Silently ignore.
                }
            }
        }
        if (priorIndex != boxPriors.length) {
            throw new RuntimeException(
                    "BoxPrior length mismatch: " + priorIndex + " vs " + boxPriors.length);
        }
    }

    private float[] decodeLocationsEncoding(final float[] locationEncoding) {
        final float[] locations = new float[locationEncoding.length];
        boolean nonZero = false;
        for (int i = 0; i < numLocations; ++i) {
            for (int j = 0; j < 4; ++j) {
                final float currEncoding = locationEncoding[4 * i + j];
                nonZero = nonZero || currEncoding != 0.0f;

                final float mean = boxPriors[i * 8 + j * 2];
                final float stdDev = boxPriors[i * 8 + j * 2 + 1];
                float currentLocation = currEncoding * stdDev + mean;
                currentLocation = Math.max(currentLocation, 0.0f);
                currentLocation = Math.min(currentLocation, 1.0f);
                locations[4 * i + j] = currentLocation;
            }
        }

        if (!nonZero) {
            //LOGGER.w("No non-zero encodings; check log for inference errors.");
        }
        return locations;
    }

    private float[] decodeScoresEncoding(final float[] scoresEncoding) {
        final float[] scores = new float[scoresEncoding.length];
        for (int i = 0; i < scoresEncoding.length; ++i) {
            scores[i] = 1 / ((float) (1 + Math.exp(-scoresEncoding[i])));
        }
        return scores;
    }

    private static Tensor<Float> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            TensorflowImageClassifier.GraphBuilder b = new TensorflowImageClassifier.GraphBuilder(g);
            // Some constants specific to the pre-trained model at:
            // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
            //
            // - The model was trained with images scaled to 224x224 pixels.
            // - The colors, represented as R, G, B in 1-byte each were converted to
            //   float using (value - Mean)/Scale.
            final int H = 224;
            final int W = 224;
            final float mean = 117f;
            final float scale = 1f;

            // Since the graph is being constructed once per execution here, we can use a constant for the
            // input image. If the graph were to be re-used for multiple input images, a placeholder would
            // have been more appropriate.
            final Output<String> input = b.constant("input", imageBytes);
            final Output<Float> output =
                    b.div(
                            b.sub(
                                    b.resizeBilinear(
                                            b.expandDims(
                                                    b.cast(b.decodeJpeg(input, 3), Float.class),
                                                    b.constant("make_batch", 0)),
                                            b.constant("size", new int[] {H, W})),
                                    b.constant("mean", mean)),
                            b.constant("scale", scale));
            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
            }
        }
    }
    
    public List<Recognition> identifyImage(byte[] imageBytes) {
        // Log this method so that it can be analyzed with systrace.
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.

        //bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        /*
        for (int i = 0; i < imageBytes.length; ++i) {
            floatValues[i * 3 + 0] = (((imageBytes[i] >> 16) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 1] = (((imageBytes[i] >> 8) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 2] = ((imageBytes[i] & 0xFF) - imageMean) / imageStd;
        }*/
        
        Tensor<Float> image = constructAndExecuteGraphToNormalizeImage(imageBytes);

        // Copy the input data into TensorFlow.
        //inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
        inferenceInterface.addFeed(inputName, image);

        // Run the inference call.
        inferenceInterface.run(outputNames, logStats);

        // Copy the output Tensor back into the output array.
        final float[] outputScoresEncoding = new float[numLocations];
        final float[] outputLocationsEncoding = new float[numLocations * 4];
        inferenceInterface.fetch(outputNames[0], outputLocationsEncoding);
        inferenceInterface.fetch(outputNames[1], outputScoresEncoding);

        outputLocations = decodeLocationsEncoding(outputLocationsEncoding);
        outputScores = decodeScoresEncoding(outputScoresEncoding);

        // Find the best detections.
        final PriorityQueue<TensorflowRecognition> pq =
                new PriorityQueue<TensorflowRecognition>(
                        1,
                        new Comparator<TensorflowRecognition>() {
                            @Override
                            public int compare(final TensorflowRecognition lhs, final TensorflowRecognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0; i < outputScores.length; ++i) {
            pq.add(new TensorflowRecognition("" + i, null, outputScores[i]));
        }

        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        for (int i = 0; i < Math.min(pq.size(), MAX_RESULTS); ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }
}