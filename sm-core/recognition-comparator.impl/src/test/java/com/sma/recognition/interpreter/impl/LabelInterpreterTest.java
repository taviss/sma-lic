package com.sma.recognition.interpreter.impl;

import com.sma.object.recognizer.api.Recognition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LabelInterpreterTest {
    private static final String TITLE = "DUMMY";
    
    @Test
    public void testBasicLabel() {
        LabelInterpreter labelInterpreter = new LabelInterpreter();
        
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
        
        List<Recognition> finalRec = labelInterpreter.interpret(recognitions1, recognition);
        assertNotNull(finalRec);
        assertEquals(1, finalRec.size());
        assertEquals(TITLE, finalRec.get(0).getTitle());
    }
}
