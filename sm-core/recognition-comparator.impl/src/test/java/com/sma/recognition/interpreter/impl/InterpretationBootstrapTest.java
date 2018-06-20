package com.sma.recognition.interpreter.impl;

import com.sma.object.recognizer.api.Recognition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class InterpretationBootstrapTest {
    private static final String TITLE = "DUMMY";
    
    @Test
    public void testBootstrapHasInterpreter() {
        AbstractRecognitionInterpreter first = InterpretationBootstrap.getInterpreter();
        assertNotNull(first);
    }
    
    @Test
    public void testChain() {
        AbstractRecognitionInterpreter first = InterpretationBootstrap.getInterpreter();

        Recognition recognition = new Recognition() {
            public String getId() {
                return null;
            }

            public String getTitle() {
                return TITLE;
            }

            public Float getConfidence() {
                return 1f;
            }

            public byte[] getSource() {
                return new byte[0];
            }
        };

        List<Recognition> recognitions1 = new ArrayList<Recognition>();
        recognitions1.add(recognition);
        
        first.interpret(recognitions1, recognition);
    }
}
