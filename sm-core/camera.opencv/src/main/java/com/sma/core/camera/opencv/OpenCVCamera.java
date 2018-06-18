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
        String osName = System.getProperty("os.name");
        String opencvpath = System.getProperty("opencv.path");
        //String opencvpath = "C:\\Users\\Tavi\\Downloads\\opencv\\build\\java";
        /*
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
        }
        else if(osName.equals("Mac OS X")){
            opencvpath = opencvpath+"Your path to .dylib";
        }*/
        System.out.println(opencvpath);
        //System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".so");
        //System.loadLibrary("opencv_ffmpeg249_64");
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

        capturedVideo.open(source);

        if (capturedVideo.isOpened()) {
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
