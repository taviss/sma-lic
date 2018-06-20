package com.sma.core.camera.st.impl;

import com.sma.core.camera.api.Camera;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImageCamera implements Camera {
    private String id;
    private BufferedImage image;
    private byte[] imageBytes;

    public ImageCamera(String id, File source) {
        this.id = id;
        try {
            this.image = ImageIO.read(source);
        } catch(IOException e) {
            throw new NullPointerException("Could not load image " + source.getAbsolutePath());
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", byteArrayOutputStream);
            this.imageBytes = byteArrayOutputStream.toByteArray();
        } catch(IOException e) {
            throw new RuntimeException("Could not convert BMP to byte array");
        }
    }
    
    public ImageCamera(String id, URL source) {
        this.id = id;
        try {
            this.image = ImageIO.read(source);
        } catch(IOException e) {
            throw new NullPointerException("Could not load image");
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", byteArrayOutputStream);
            this.imageBytes = byteArrayOutputStream.toByteArray();
        } catch(IOException e) {
            throw new RuntimeException("Could not convert BMP to byte array");
        }
    }

    public byte[] getSnapshot() {
        return this.imageBytes;
    }

    public String getId() {
        return this.id;
    }
}
