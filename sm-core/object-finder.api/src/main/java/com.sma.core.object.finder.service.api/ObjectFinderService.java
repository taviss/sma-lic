package com.sma.core.object.finder.service.api;

import com.sma.core.camera.api.Camera;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;

import java.util.List;

/**
 * A class that knows how to identify objects using a set of cameras and object recognizers/classifiers
 */
public interface ObjectFinderService {
    /**
     * Bind the object recognizer used to identify the image to from the camera
     * @param objectRecognizer
     */
    void bindObjectRecoginzer(ObjectRecognizer objectRecognizer);

    /**
     * Bind the object recognizer used to identify the image to look for
     * @param objectRecognizer
     */
    void bindImageClassifier(ObjectRecognizer objectRecognizer);

    /**
     * Returns a list of images as byte arrays in which the object being looked for appears
     * @param imageBytes
     * @return
     */
    List<Recognition> findObject(byte[] imageBytes);

    /**
     * Adds a camera to this object finder
     * @param camera
     */
    void addCamera(Camera camera);
}
