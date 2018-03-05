import com.sma.object.finder.tf.TensorflowImageClassifier;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TestTFImageClassifier {

    @Test
    public void testImg() throws IOException, URISyntaxException {
        
        //String mDir = "D:\\Facultate\\LIC_SMA\\sm-core\\object-recognizer\\src\\main\\resources";
        BufferedImage bufferedImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("puppy_224.jpg"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
        
        URI tfModel = getClass().getClassLoader().getResource("tensorflow_inception_graph.pb").toURI();
        URI tfLabels = getClass().getClassLoader().getResource("imagenet_comp_graph_label_strings.txt").toURI();

        TensorflowImageClassifier tensorflowImageClassifier = new TensorflowImageClassifier(tfModel, tfLabels);
        tensorflowImageClassifier.identifyImage(byteArrayOutputStream.toByteArray());
    }
}
