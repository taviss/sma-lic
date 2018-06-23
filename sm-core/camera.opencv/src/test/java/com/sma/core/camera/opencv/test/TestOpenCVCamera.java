package com.sma.core.camera.opencv.test;

import com.sma.core.camera.opencv.OpenCVCamera;
import org.junit.Test;

public class TestOpenCVCamera {
    
    //@Test
    public void testCamera() {
        System.out.println("Test");
        OpenCVCamera openCVCamera = new OpenCVCamera("1", "http://admin:admin123@193.226.12.217:8888/Streaming/Channels/1/picture");
        openCVCamera.getSnapshot();
    }
}
