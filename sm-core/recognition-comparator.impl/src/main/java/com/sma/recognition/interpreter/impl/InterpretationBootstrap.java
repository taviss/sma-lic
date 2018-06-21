package com.sma.recognition.interpreter.impl;

/**
 * Helper class that holds the chain of interpreters
 */
public class InterpretationBootstrap {
    private static AbstractRecognitionInterpreter rootInterpreter;

    static {
        rootInterpreter = new LabelInterpreter();
    }
    
    public static AbstractRecognitionInterpreter getInterpreter() {
        return rootInterpreter;
    }
}
