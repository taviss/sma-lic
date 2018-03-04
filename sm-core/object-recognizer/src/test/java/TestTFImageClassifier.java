import com.sma.object.finder.tf.TensorflowImageClassifier;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class TestTFImageClassifier {

    @Test
    public void testImg() throws IOException {

        String mDir = "D:\\Facultate\\LIC_SMA\\sm-core\\object-recognizer\\src\\main\\resources";
        BufferedImage bufferedImage = ImageIO.read(new File("D:\\Facultate\\LIC_SMA\\sm-core\\object-recognizer\\src\\main\\resources\\puppy_224.jpg"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);

        TensorflowImageClassifier tensorflowImageClassifier = new TensorflowImageClassifier(mDir);
        tensorflowImageClassifier.identifyImage(byteArrayOutputStream.toByteArray());
    }
}
