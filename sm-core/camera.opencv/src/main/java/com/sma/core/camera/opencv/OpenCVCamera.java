package com.sma.core.camera.opencv;

import com.sma.core.camera.api.Camera;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * {@inheritDoc}
 */
public class OpenCVCamera implements Camera {
    private static final Logger LOG = LoggerFactory.getLogger(OpenCVCamera.class.getName());
    
    /**
     * This camera's ID
     */
    private String id;

    /**
     * The source(address)
     */
    private String source;
    
    // Static loading of OpenCV
    static {
        String osName = System.getProperty("os.name");
        //String opencvpath = System.getProperty("opencv.path");
        String opencvpath = "C:\\Users\\Tavi\\Downloads\\opencv\\build\\java";

        if(osName.startsWith("Windows")) {
            int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
            if(bitness == 32) {
                opencvpath=opencvpath+"\\x86\\";
            }
            else if (bitness == 64) {
                opencvpath=opencvpath+"\\x64\\";
            } else {
                opencvpath=opencvpath+"\\x86\\";
            }
            LOG.info(opencvpath);
            System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
            System.loadLibrary("opencv_ffmpeg249_64");
        } else {
            OpenCV.loadLibrary();
        }
        
    }
    
    public OpenCVCamera(String id, String source) {
        this.id = id;
        this.source = source;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public byte[] getSnapshot() {
        // Capture a frame
        Mat mat = new Mat();
        VideoCapture capturedVideo = new VideoCapture();

        capturedVideo.open(source);

        if (capturedVideo.isOpened()) {
            if(capturedVideo.read(mat)) {
                if (!mat.empty()) {
                    // Transform the frame to a JPEG image and return it as a byte array.
                    MatOfByte mob=new MatOfByte();
                    Highgui.imencode(".jpg", mat, mob);
                    return mob.toArray();
                }
            } else {
                LOG.warn("Mat is empty.");
            }
        } else {
            LOG.warn("Camera connection problem. Check addressString");
        }
        
        return new byte[0];
    }

    @Override
    public String getId() {
        return this.id;
    }
}
