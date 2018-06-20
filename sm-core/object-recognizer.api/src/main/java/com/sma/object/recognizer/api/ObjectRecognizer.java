package com.sma.object.recognizer.api;

import java.util.List;

/**
 * A class capable of identifying objects in a given image
 */
public interface ObjectRecognizer {
    /**
     * Identify objects inside an image
     * @param imageBytes the image
     * @return a list of {@link Recognition}
     */
   List<Recognition> identifyImage(byte[] imageBytes);
}
