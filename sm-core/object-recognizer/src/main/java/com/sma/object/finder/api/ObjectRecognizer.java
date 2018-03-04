package com.sma.object.finder.api;

import java.util.List;

public interface ObjectRecognizer {
   List<String> identifyImage(byte[] imageBytes);
}
