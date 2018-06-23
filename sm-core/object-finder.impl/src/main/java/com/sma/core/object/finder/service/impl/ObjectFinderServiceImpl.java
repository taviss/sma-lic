package com.sma.core.object.finder.service.impl;

import com.sma.core.camera.api.Camera;
import com.sma.core.object.finder.service.api.ObjectFinderService;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import com.sma.recognition.interpreter.impl.InterpretationBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * {@inheritDoc}
 */
public class ObjectFinderServiceImpl implements ObjectFinderService{
    private static final Logger LOG = LoggerFactory.getLogger(ObjectFinderServiceImpl.class);
    
    /**
     * The list of cameras this {@link ObjectFinderService} has access to
     */
    private List<Camera> cameraNetwork;

    /**
     * The {@link ObjectRecognizer} used for identifying objects in the {@link Camera} snapshot
     */
    private ObjectRecognizer objectRecognizer;

    /**
     * The {@link ObjectRecognizer} used for identifying objects in the given image
     */
    private ObjectRecognizer imageClassifier;

    /**
     * Minimum confidence for recognized objects
     */
    private static final float MINIMUM_CONFIDENCE = 0.7f;

    public ObjectFinderServiceImpl() {
        this.cameraNetwork = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     * @param camera
     */
    public void addCamera(Camera camera) {
        this.cameraNetwork.add(camera);
    }

    /**
     * {@inheritDoc}
     * @param objectRecognizer
     */
    public void bindObjectRecoginzer(ObjectRecognizer objectRecognizer) {
        this.objectRecognizer = objectRecognizer;
    }

    /**
     * {@inheritDoc}
     * @param objectRecognizer
     */
    public void bindImageClassifier(ObjectRecognizer objectRecognizer) {
        this.imageClassifier = objectRecognizer;
    }

    /**
     * {@inheritDoc}
     * @param imageBytes
     * @return
     */
    public List<Recognition> findObject(byte[] imageBytes) {
        LOG.debug("findObject()");
        List<Recognition> globalRecognitions = new ArrayList<>();
        
        float bestGlobalConfidence = 0.0f;
        Recognition bestMatch = null;
        
        for(Camera camera : cameraNetwork) {
            byte[] cameraSnapshot = camera.getSnapshot();
            
            List<Recognition> results = objectRecognizer.identifyImage(cameraSnapshot);
            List<Recognition> imageIdentification = imageClassifier.identifyImage(imageBytes);
            if(results != null && results.size() > 0 && imageIdentification != null && imageIdentification.size() > 0) {
                imageIdentification.sort(new ConfidenceComparator().reversed());

                List<Recognition> interpreted = InterpretationBootstrap.getInterpreter().interpret(results, imageIdentification.get(0));

                globalRecognitions.addAll(interpreted);
            }
            /*
            for(Recognition recognition : interpreted) {
                if(bestGlobalConfidence < recognition.getConfidence()) {
                    bestGlobalConfidence = recognition.getConfidence();
                    bestMatch = recognition;
                    globalRecognitions.clear();
                } else if (bestGlobalConfidence == recognition.getConfidence()) {
                    globalRecognitions.add(recognition);
                }
            }*/
        }
        /*
        if(bestMatch != null) {
            globalRecognitions.add(bestMatch);
        }*/
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
