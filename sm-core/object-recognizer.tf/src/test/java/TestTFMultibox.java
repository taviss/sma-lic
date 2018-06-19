
import com.sma.core.camera.opencv.OpenCVCamera;
import com.sma.object.finder.tf.TensorflowObjectDetectionAPI;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class TestTFMultibox {
    private static final int MB_INPUT_SIZE = 224;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final String MB_MODEL_FILE = "D:/Facultate/LIC_SMA/sma-lic/sm-core/object-recognizer.tf/src/main/resources/ssd_mobilenet_v1_android_export.pb";
    private static final String MB_LOCATION_FILE =
            "D:/Facultate/LIC_SMA/sma-lic/sm-core/object-recognizer.tf/src/main/resources/coco_labels_list.txt";
    
    @Test
    public void testMB() throws Exception {
        BufferedImage bufferedImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("puppy_224.jpg"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
        
        ObjectRecognizer tensorflowMultibox = TensorflowObjectDetectionAPI.create(
                MB_MODEL_FILE,
                MB_LOCATION_FILE,
                300
                );
        tensorflowMultibox.identifyImage(byteArrayOutputStream.toByteArray());
    }

    @Test
    public void testAPI() throws Exception {
        BufferedImage bufferedImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("mouse.jpg"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);

        ObjectRecognizer tensorflowMultibox = TensorflowObjectDetectionAPI.create(
                MB_MODEL_FILE,
                MB_LOCATION_FILE,
                300
        );

        List<Recognition> recognitions = tensorflowMultibox.identifyImage(byteArrayOutputStream.toByteArray());
        for(Recognition recognition : recognitions) {
            if(recognition.getConfidence() > 0.7) {
                System.out.println(recognition.getTitle() + " : " + recognition.getConfidence());
            }
        }


        System.out.println("OpenCV recognitions:");

        OpenCVCamera openCVCamera = new OpenCVCamera("1", "http://192.168.137.1:8080/live?dummy=x.mjpg");
        byte[] opencvImage = openCVCamera.getSnapshot();

        List<Recognition> recognitionsO = tensorflowMultibox.identifyImage(opencvImage);
        for(Recognition recognition : recognitionsO) {
            System.out.println(recognition.getTitle() + " : " + recognition.getConfidence());
        }

    }
}
