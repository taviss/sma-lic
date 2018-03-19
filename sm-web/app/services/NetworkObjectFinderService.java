package services;

import com.sma.core.camera.api.Camera;
import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.ObjectFinderService;
import com.sma.object.finder.api.ObjectRecognizer;
import com.typesafe.config.Config;
import models.CameraAddress;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

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
    
    //TODO Async
    public String findObject(Long userId, List<Camera> cameras, byte[] imageBytes) {
        synchronized (cache) {
            ObjectFinderService objectFinderService = cache.get(userId);
            if(objectFinderService == null) {
                objectFinderService = new ObjectFinderService();
                objectFinderService.bindObjectRecoginzer(this.objectRecognizer);
                for(Camera camera : cameras) {
                    objectFinderService.addCamera(camera);
                }
                cache.put(userId, objectFinderService);
            }
            
            objectFinderService.findObject(imageBytes);
        }
        return "";
    }
}
