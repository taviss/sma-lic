package com.sma.core.object.finder.service.impl;

import com.sma.core.camera.api.Camera;
import com.sma.core.object.finder.service.api.ObjectFinderService;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ObjectFinderServiceImpl implements ObjectFinderService{
    private List<Camera> cameraNetwork;
    private ObjectRecognizer objectRecognizer;
    private ObjectRecognizer imageClassifier;
    
    private static final float MINIMUM_CONFIDENCE = 0.7f;

    public ObjectFinderServiceImpl() {
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

    public List<Recognition> findObject(byte[] imageBytes) {
        List<Recognition> globalRecognitions = new ArrayList<>();
        
        float bestGlobalConfidence = 0.0f;
        Recognition bestMatch = null;
        
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
                        bestMatch = recognition;
                        globalRecognitions.clear();
                    } else if (bestGlobalConfidence == recognition.getConfidence()) {
                        globalRecognitions.add(recognition);
                    }
                }
            }
        }
        if(bestMatch != null) {
            globalRecognitions.add(bestMatch);
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
