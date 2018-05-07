package com.sma.object.finder.tf;

import com.sma.object.recognizer.api.Recognition;

public class TensorflowRecognition implements Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /**
     * Display name for the recognition.
     */
    private final String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float confidence;
    
    private final byte[] source;

    public TensorflowRecognition(
            final String id, final String title, final Float confidence, final byte[] source) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence;
    }

    @Override
    public byte[] getSource() {
        return source;
    }

    @Override
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
