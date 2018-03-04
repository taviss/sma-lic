package com.sma.core.object.finder.service;

import com.sma.core.camera.api.Camera;
import com.sma.object.finder.api.ObjectRecognizer;

import java.util.ArrayList;
import java.util.List;

public class ObjectFinderService {
    private List<Camera> cameraNetwork;
    private ObjectRecognizer objectRecognizer;

    public ObjectFinderService() {
        this.cameraNetwork = new ArrayList<>();
    }

    public void addCamera(Camera camera) {
        this.cameraNetwork.add(camera);
    }

    public void bindObjectRecoginzer(ObjectRecognizer objectRecognizer) {
        this.objectRecognizer = objectRecognizer;
    }

    public void findObject(byte[] imageBytes) {
        for(Camera camera : cameraNetwork) {
            List<String> results = objectRecognizer.identifyImage(camera.getSnapshot());

            for(String result : results) {
                System.out.println(result);
            }
        }
    }
}
