
import com.sma.core.camera.opencv.OpenCVCamera;
import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.object.finder.tf.TensorflowObjectDetectionAPI;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class TestTFMultibox {
    private static final int MB_INPUT_SIZE = 224;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final String MB_MODEL_FILE = "ssd_mobilenet_v1_android_export.pb";
    private static final String MB_LOCATION_FILE =
            "coco_labels_list.txt";
    
    @Test
    public void testMB() throws Exception {
        BufferedImage bufferedImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("puppy_224.jpg"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);

        URL model = getClass().getClassLoader().getResource(MB_MODEL_FILE);
        File modelFile = new File(model.toURI());

        URL labels = getClass().getClassLoader().getResource(MB_LOCATION_FILE);
        File labelsFile = new File(labels.toURI());
        
        ObjectRecognizer tensorflowMultibox = TensorflowObjectDetectionAPI.create(
                modelFile.getAbsolutePath(),
                labelsFile.getAbsolutePath(),
                300
                );
        tensorflowMultibox.identifyImage(byteArrayOutputStream.toByteArray());
    }

    @Test
    public void testAPI() throws Exception {

        BufferedImage bufferedImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("mouse.jpg"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);

        URL model = getClass().getClassLoader().getResource(MB_MODEL_FILE);
        File modelFile = new File(model.toURI());

        URL labels = getClass().getClassLoader().getResource(MB_LOCATION_FILE);
        File labelsFile = new File(labels.toURI());

        ObjectRecognizer tensorflowMultibox = TensorflowObjectDetectionAPI.create(
                modelFile.getAbsolutePath(),
                labelsFile.getAbsolutePath(),
                300
        );

        /*
        List<Recognition> recognitions = tensorflowMultibox.identifyImage(byteArrayOutputStream.toByteArray());
        for(Recognition recognition : recognitions) {
            if(recognition.getConfidence() > 0.7) {
                System.out.println(recognition.getTitle() + " : " + recognition.getConfidence());
            }
        }*/



        System.out.println("OpenCV recognitions:");

        URL img = new URL("http://admin:admin123@193.226.12.217:8888/Streaming/Channels/1/picture");
        ImageCamera imageCamera = new ImageCamera("1", img);
        byte[] getImg = imageCamera.getSnapshot();



        ByteArrayInputStream in = new ByteArrayInputStream(getImg);
        BufferedImage bufferedImage1 = ImageIO.read(in);
        int size = bufferedImage1.getHeight() > bufferedImage1.getWidth() ? bufferedImage1.getWidth() : bufferedImage1.getHeight();
        ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
        bufferedImage1 = bufferedImage1.getSubimage(0, 0, size, size);
        ImageIO.write(bufferedImage1, "jpeg", byteArrayOutputStream1);
        getImg = byteArrayOutputStream1.toByteArray();

        List<Recognition> recognitionsO = tensorflowMultibox.identifyImage(getImg);
        for(Recognition recognition : recognitionsO) {
            if(recognition.getConfidence() > 0.5f) {
                ByteArrayInputStream bais = new ByteArrayInputStream(recognition.getSource());
                BufferedImage image = ImageIO.read(bais);
                File outputfile = new File("D:/" + recognition.getTitle() + recognition.getId() + ".jpg");
                ImageIO.write(image, "jpg", outputfile);

                ByteArrayInputStream bais1 = new ByteArrayInputStream(recognition.getBoundedObject());
                BufferedImage boundedImage = ImageIO.read(bais1);
                File outputfile2 = new File("D:/" + recognition.getTitle() + recognition.getId() + "_bounded.jpg");
                ImageIO.write(boundedImage, "jpg", outputfile2);

                List<Recognition> boundedRec = tensorflowMultibox.identifyImage(recognition.getBoundedObject(), Integer.min(boundedImage.getHeight(), boundedImage.getWidth()));

                for(Recognition boundedRecog : boundedRec) {
                    if(boundedRecog.getConfidence() > 0.5f) {
                        System.out.println("BOUNDED: " + boundedRecog.getTitle());
                    }
                }
            }
            System.out.println(recognition.getTitle() + " : " + recognition.getConfidence());
        }

    }
}
