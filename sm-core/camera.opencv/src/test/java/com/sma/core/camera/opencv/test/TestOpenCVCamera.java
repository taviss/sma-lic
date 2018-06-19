package com.sma.core.camera.opencv.test;

import com.sma.core.camera.opencv.OpenCVCamera;
import org.junit.Test;

public class TestOpenCVCamera {
    
    @Test
    public void testCamera() {
        System.out.println("Test");
        OpenCVCamera openCVCamera = new OpenCVCamera("1", "http://192.168.137.1:8080/live?dummy=x.mjpg");
        openCVCamera.getSnapshot();
    }
}
