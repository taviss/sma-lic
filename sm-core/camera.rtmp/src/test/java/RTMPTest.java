import com.sma.core.camera.rtmp.RTMPCamera;
import org.junit.Test;

public class RTMPTest {
    private static final String HOST = "184.72.239.149";
    private static final String PATH = "vod/mp4:bigbuckbunny_1500.mp4";
//rtmp:flash.oit.duke.edu/vod/_definst_

    @Test
    public void testRTMPClient() {
        RTMPCamera rtmpCamera = new RTMPCamera("1", HOST, PATH, 1935);
        rtmpCamera.getSnapshot();
    }
}
