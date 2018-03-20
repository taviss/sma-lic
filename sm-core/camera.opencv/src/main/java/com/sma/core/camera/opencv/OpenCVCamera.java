package com.sma.core.camera.opencv;

import com.sma.core.camera.api.Camera;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class OpenCVCamera implements Camera {

    private String id;
    private String source;
    
    static {
        OpenCV.loadLibrary();
    }
    
    public OpenCVCamera(String id, String source) {
        this.id = id;
        this.source = source;
    }
    
    @Override
    public byte[] getSnapshot() {
        Mat mat = new Mat();
        VideoCapture capturedVideo = new VideoCapture();

        boolean isOpened = capturedVideo.open(source);

        if (isOpened) {
            boolean tempBool = capturedVideo.read(mat);
            System.out.println("VideoCapture returned mat? "+tempBool);

            if (!mat.empty()) {
                System.out.println("Print image size: "+mat.size());
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 0);
                
                BufferedImage gray = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
                
                byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
                mat.get(0, 0, data);
                return data;
            } else {
                System.out.println("Mat is empty.");
            }
        } else {
            System.out.println("Camera connection problem. Check addressString");
        }
        
        return new byte[0];
    }

    @Override
    public String getId() {
        return null;
    }
}
