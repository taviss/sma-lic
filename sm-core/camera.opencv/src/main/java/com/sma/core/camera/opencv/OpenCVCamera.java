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
        System.out.println("OS=" + osName);

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
                    // Reshape the image to a square shape
                    Mat resizedImage = new Mat();
                    double size = mat.size().height > mat.size().width ? mat.size().width : mat.size().height;
                    Size sqr = new Size(size, size);
                    Imgproc.resize(mat, resizedImage, sqr);
                    // Transform the frame to a JPEG image and return it as a byte array.
                    MatOfByte mob=new MatOfByte();
                    Highgui.imencode(".jpg", resizedImage, mob);
                    return mob.toArray();
                }
            } else {
                LOG.warn("Mat is empty.");
            }
        } else {
            System.out.println("Camera connection problem. Check addressString");
        }
        
        return new byte[0];
    }

    @Override
    public String getId() {
        return this.id;
    }
}
