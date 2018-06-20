package com.sma.recognition.interpreter.impl;

import com.sma.object.recognizer.api.Recognition;
import com.sma.recognition.interpreter.api.RecognitionInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Interpreter that compares labels (strings)
 */
public class LabelInterpreter extends AbstractRecognitionInterpreter implements RecognitionInterpreter {
    private static final Logger LOG = LoggerFactory.getLogger(LabelInterpreter.class);

    /**
     * {@inheritDoc}
     * @param cameraRecognitions
     * @param imageRecognition
     * @return
     */
    @Override
    public List<Recognition> interpretRecognitions(List<Recognition> cameraRecognitions, Recognition imageRecognition) {
        List<Recognition> globalRecognitions = new ArrayList<Recognition>();
        for(Recognition recognition : cameraRecognitions) {
            if(recognition.getConfidence() < MINIMUM_CONFIDENCE) {
                LOG.debug("Recognition" + recognition.getTitle() + " skipped due to low confidence.");
                continue;
            }

            if(recognition.getTitle().equals(imageRecognition.getTitle())) {
                    globalRecognitions.add(recognition);
            }
        }
        return globalRecognitions;
    }
}
