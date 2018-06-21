package services;

import com.sma.core.camera.api.Camera;
import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.api.ObjectFinderService;
import com.sma.core.object.finder.service.impl.ObjectFinderServiceImpl;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import com.typesafe.config.Config;
import models.CameraAddress;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * Service for searching for an object
 */
@Singleton
public class NetworkObjectFinderService {
    
    private static final HashMap<Long, ObjectFinderService> cache = new HashMap<>();
    
    private final ObjectRecognizer objectRecognizer;
    
    @Inject
    private Config config;
    
    @Inject
    public NetworkObjectFinderService(ObjectRecognizer objectRecognizer) {
        this.objectRecognizer = objectRecognizer;
    }
    
    //TODO Should this be in a different thread?
    public List<Recognition> findObject(Long userId, List<Camera> cameras, byte[] imageBytes) {
        synchronized (cache) {
            ObjectFinderService objectFinderService = cache.get(userId);
            if(objectFinderService == null) {
                objectFinderService = new ObjectFinderServiceImpl();
                objectFinderService.bindObjectRecoginzer(this.objectRecognizer);
                objectFinderService.bindImageClassifier(this.objectRecognizer);
                for(Camera camera : cameras) {
                    objectFinderService.addCamera(camera);
                }
                cache.put(userId, objectFinderService);
            }
            
            return objectFinderService.findObject(imageBytes);
        }
    }
}
