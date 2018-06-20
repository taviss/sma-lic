package com.sma.object.recognizer.api;

/**
 * A recognition containing a label for the recognized object
 */
public interface Recognition {
    /** Get the id
     * @return
     */
    String getId();

    /**
     * Get the label
     * @return
     */
    String getTitle();

    /**
     * Get the confidence
     * @return
     */
    Float getConfidence();

    /**
     * The source of the recognition (image)
     * @return
     */
    byte[] getSource();
}
