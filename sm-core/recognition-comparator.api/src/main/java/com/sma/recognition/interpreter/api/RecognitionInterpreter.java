package com.sma.recognition.interpreter.api;

import com.sma.object.recognizer.api.Recognition;

import java.util.List;

/**
 * A class capable of interpreting a series of recognitions
 */
public interface RecognitionInterpreter {
    /**
     * Compare recognitions
     * @param cameraRecognitions
     * @param imageRecognition
     * @return
     */
    List<Recognition> interpretRecognitions(List<Recognition> cameraRecognitions, Recognition imageRecognition);
}
