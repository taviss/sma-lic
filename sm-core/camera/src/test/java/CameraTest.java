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
}
