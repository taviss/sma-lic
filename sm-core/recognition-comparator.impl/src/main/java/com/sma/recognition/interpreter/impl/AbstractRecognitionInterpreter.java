package com.sma.recognition.interpreter.impl;

import com.sma.object.recognizer.api.Recognition;
import com.sma.recognition.interpreter.api.RecognitionInterpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class that uses chain of responsability pattern to delegate recognition comparison
 */
public abstract class AbstractRecognitionInterpreter {
    public static final float MINIMUM_CONFIDENCE = 0.7f;
    
    protected AbstractRecognitionInterpreter nextInterpreter;
    
    public void setNext(AbstractRecognitionInterpreter recognitionInterpreter) {
        this.nextInterpreter = recognitionInterpreter;
    }
    
    public List<Recognition> interpret(List<Recognition> cameraRecognitions, Recognition imageRecognitions) {
        List<Recognition> recognitions = new ArrayList<Recognition>();
        for(Recognition recognition : interpretRecognitions(cameraRecognitions, imageRecognitions)) {
            if(!recognitions.contains(recognition))
                recognitions.add(recognition);
        }
        
        if(nextInterpreter != null) {
            List<Recognition> nextRecognitions = nextInterpreter.interpret(cameraRecognitions, imageRecognitions);
            for(Recognition recognition : nextRecognitions) {
                if(!recognitions.contains(recognition))
                    recognitions.add(recognition);
            }
        }
        return recognitions;
    }
    
    abstract protected  List<Recognition> interpretRecognitions(List<Recognition> cameraRecognitions, Recognition imageRecognition);
}
