package com.sma.object.finder.api;

import com.sma.object.finder.tf.Recognition;

import java.util.List;

public interface ObjectRecognizer {
   List<Recognition> identifyImage(byte[] imageBytes);
}
