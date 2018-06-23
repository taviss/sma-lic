package com.sma.core.camera.st.impl;

import com.sma.core.camera.api.Camera;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ImageCamera implements Camera {
    /**
     * The ID of this camera
     */
    private String id;

    /**
     * A buffered image to get snapshot from
     */
    private BufferedImage image;

    /**
     * Static or might change?
     */
    private boolean isStatic;

    /**
     * The source if non static
     */
    private URL source;

    /**
     * The image as byte array
     */
    private byte[] imageBytes;

    public ImageCamera(String id, File source) {
        this.id = id;
        // From file
        this.isStatic = true;
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
        this.source = source;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public byte[] getSnapshot() {
        if (isStatic) {
            return this.imageBytes;
        } else {
            try {
                URLConnection uc = source.openConnection();

                String auth = source.getAuthority();
                String userpass = auth.split("@")[0];
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

                uc.setRequestProperty ("Authorization", basicAuth);

                InputStream is = uc.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] b = new byte[2048];
                int length;

                while ((length = is.read(b)) != -1) {
                    byteArrayOutputStream.write(b, 0, length);
                }
                is.close();
                byteArrayOutputStream.close();

                // Resize the image to a square shape
                this.imageBytes = byteArrayOutputStream.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                this.image = ImageIO.read(bais);
                int size = image.getHeight() > image.getWidth() ? image.getWidth() : image.getHeight();
                this.image = image.getSubimage(0, 0, size, size);

                ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                ImageIO.write(image, "jpeg", byteArrayOutputStream2);
                this.imageBytes = byteArrayOutputStream2.toByteArray();
                return this.imageBytes;
            } catch(IOException e) {
                throw new NullPointerException("Could not load image");
            }
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public String getId() {
        return this.id;
    }
}
