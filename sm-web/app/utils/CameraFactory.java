package utils;

import com.sma.core.camera.api.Camera;
import com.sma.core.camera.opencv.OpenCVCamera;
import com.sma.core.camera.rtmp.RTMPCamera;
import com.sma.core.camera.st.impl.ImageCamera;
import models.CameraAddress;

import java.io.File;

public class CameraFactory {
    
    public static Camera createCamera(CameraAddress cameraAddress) {
        switch(cameraAddress.getCameraType()) {
            case RTMP: {
                String server = cameraAddress.getAddress().split("/")[0];
                String path = cameraAddress.getAddress().split("/")[1].split(":")[0];
                String port = cameraAddress.getAddress().split(":")[1];
                return createRTMPCamera(String.valueOf(cameraAddress.getId()), server, path, Integer.valueOf(port));
            }
            case OPENCV: {
                return createOpenCVCamera(String.valueOf(cameraAddress.getId()), cameraAddress.getAddress());
            }
            case STATIC: {
                return createStaticCamera(String.valueOf(cameraAddress.getId()), cameraAddress.getAddress());
            }
            default:
                return null;
        }
    }
    
    public static ImageCamera createStaticCamera(String id, String path) {
        return new ImageCamera(id, new File(path));
    }
    
    public static OpenCVCamera createOpenCVCamera(String id, String res) {
        return new OpenCVCamera(id, res);
    }
    
    public static RTMPCamera createRTMPCamera(String id, String server, String path, int port) {
        return new RTMPCamera(id, server, path, port);
    }
}
