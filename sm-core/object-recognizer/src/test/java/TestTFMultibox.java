import com.sma.object.finder.api.ObjectRecognizer;
import com.sma.object.finder.tf.TensorflowMultibox;
import com.sma.object.finder.tf.TensorflowObjectDetectionAPI;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class TestTFMultibox {
    private static final int MB_INPUT_SIZE = 224;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final String MB_MODEL_FILE = "src/main/resources/ssd_mobilenet_v1_android_export.pb";
    private static final String MB_LOCATION_FILE =
            "src/main/resources/coco_labels_list.txt";
    
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
}
