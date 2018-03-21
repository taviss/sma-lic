package com.sma.core.object.finder.service;

import com.sma.core.camera.api.Camera;
import com.sma.object.finder.api.ObjectRecognizer;
import com.sma.object.finder.tf.Recognition;
import com.sma.object.finder.tf.TensorflowImageClassifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ObjectFinderService {
    private List<Camera> cameraNetwork;
    private ObjectRecognizer objectRecognizer;
    private ObjectRecognizer imageClassifier;
    
    private static final float MINIMUM_CONFIDENCE = 0.7f;

    public ObjectFinderService() {
        this.cameraNetwork = new ArrayList<>();
    }

    public void addCamera(Camera camera) {
        this.cameraNetwork.add(camera);
    }

    public void bindObjectRecoginzer(ObjectRecognizer objectRecognizer) {
        this.objectRecognizer = objectRecognizer;
    }
    
    public void bindImageClassifier(ObjectRecognizer objectRecognizer) {
        this.imageClassifier = objectRecognizer;
    }

    public List<byte[]> findObject(byte[] imageBytes) {
        List<byte[]> globalRecognitions = new ArrayList<>();
        
        float bestGlobalConfidence = 0.0f;
        byte[] bestMatchSnapshot = null;
        
        for(Camera camera : cameraNetwork) {
            byte[] cameraSnapshot = camera.getSnapshot();
            
            List<Recognition> results = objectRecognizer.identifyImage(cameraSnapshot);
            List<Recognition> imageIdentification = imageClassifier.identifyImage(imageBytes);
            imageIdentification.sort(new ConfidenceComparator().reversed());
            
            //TODO Add box
            for(Recognition recognition : results) {
                if(recognition.getConfidence() < MINIMUM_CONFIDENCE)
                    continue;
                
                if(recognition.getTitle().equals(imageIdentification.get(0).getTitle())) {
                    if(bestGlobalConfidence < recognition.getConfidence()) {
                        bestGlobalConfidence = recognition.getConfidence();
                        bestMatchSnapshot = cameraSnapshot;
                        globalRecognitions.clear();
                    } else if (bestGlobalConfidence == recognition.getConfidence()) {
                        globalRecognitions.add(cameraSnapshot);
                    }
                }
            }
        }
        if(bestMatchSnapshot != null) {
            globalRecognitions.add(bestMatchSnapshot);
        }
        return globalRecognitions;
    }
    
    public static final class ConfidenceComparator implements Comparator<Recognition> {

        @Override
        public int compare(Recognition o1, Recognition o2) {
            float conf1 = o1.getConfidence();
            float conf2 = o2.getConfidence();
            
            return Float.compare(conf1, conf2);
        }
    }
}
