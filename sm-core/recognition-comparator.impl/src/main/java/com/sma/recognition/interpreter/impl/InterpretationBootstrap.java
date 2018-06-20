package com.sma.recognition.interpreter.impl;

public class InterpretationBootstrap {
    private static AbstractRecognitionInterpreter rootInterpreter;

    static {
        rootInterpreter = new LabelInterpreter();
    }
    
    public static AbstractRecognitionInterpreter getInterpreter() {
        return rootInterpreter;
    }
}
