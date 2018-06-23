import com.sma.core.camera.st.impl.ImageCamera;
import org.junit.Test;

import java.net.URL;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class CameraTest {
    
    @Test
    public void testImageCamera() {
        URL img = CameraTest.class.getClassLoader().getResource("puppy_224.jpg");
        ImageCamera imageCamera = new ImageCamera("1", img);
        assertEquals("1", imageCamera.getId());
        assertTrue(imageCamera.getSnapshot() != null);
    }

    @Test
    public void testURLCamera() throws Exception {
        URL img = new URL("http://admin:admin123@193.226.12.217:8888/Streaming/Channels/1/picture");
        ImageCamera imageCamera = new ImageCamera("1", img);
        assertEquals("1", imageCamera.getId());
        assertTrue(imageCamera.getSnapshot() != null);
    }
}
