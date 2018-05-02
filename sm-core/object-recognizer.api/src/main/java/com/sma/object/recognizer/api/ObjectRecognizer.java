package com.sma.object.recognizer.api;

import java.util.List;

public interface ObjectRecognizer {
   List<Recognition> identifyImage(byte[] imageBytes);
}
