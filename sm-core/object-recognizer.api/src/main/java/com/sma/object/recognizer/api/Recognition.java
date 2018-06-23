package com.sma.object.recognizer.api;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A recognition containing a label for the recognized object
 */
public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private String id;

    /**
     * Display name for the recognition.
     */
    private String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private Float confidence;

    private byte[] source;

    private byte[] boundedObject;

    // Bounding box
    private int left;
    private int top;
    private int right;
    private int bottom;

    public Recognition() {

    }

    public Recognition(
            final String id, final String title, final Float confidence, final byte[] source,
            final int left, final int top, final int right, final int bottom) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(source);
            BufferedImage image = ImageIO.read(bais);

            int width = right - left;
            int height = bottom - top;

            if(width != 0 && height != 0) {
                BufferedImage cropped = image.getSubimage(left, top, width, height);
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                ImageIO.write( cropped, "jpg", baos2 );
                this.boundedObject = baos2.toByteArray();
            }

            // Draw bounding box
            Graphics2D graph = image.createGraphics();
            graph.setColor(Color.RED);
            graph.setStroke(new BasicStroke(5f));
            graph.drawRect(left, top, width, height);
            graph.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( image, "jpg", baos );
            this.source = baos.toByteArray();


        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /** Get the id
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Get the label
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the confidence
     * @return
     */
    public Float getConfidence() {
        return confidence;
    }

    /**
     * The source of the recognition (image)
     * @return
     */
    public byte[] getSource() {
        return source;
    }

    /**
     * Returns a sub-image including only the object
     * @return
     */
    public byte[] getBoundedObject() {

        return this.boundedObject;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    public String toString() {
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);
        }

        return resultString.trim();
    }
}

