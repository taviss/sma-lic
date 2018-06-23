package com.sma.core.boject.finder.service.impl;

import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.impl.ObjectFinderServiceImpl;
import com.sma.object.finder.tf.TensorflowObjectDetectionAPI;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ObjectFinderTest {
    private static final String MB_MODEL_FILE = "ssd_mobilenet_v1_android_export.pb";
    private static final String MB_LOCATION_FILE =
            "coco_labels_list.txt";
    
    @Test
    public void testCameraAdd() {
        ObjectFinderServiceImpl objectFinderService = new ObjectFinderServiceImpl();
        objectFinderService.addCamera(new MockCamera());
    }

    @Test
    public void testReal() throws Exception {
        URL model = getClass().getClassLoader().getResource(MB_MODEL_FILE);
        File modelFile = new File(model.toURI());

        URL labels = getClass().getClassLoader().getResource(MB_LOCATION_FILE);
        File labelsFile = new File(labels.toURI());

        ObjectRecognizer tensorflowMultibox = TensorflowObjectDetectionAPI.create(
                modelFile.getAbsolutePath(),
                labelsFile.getAbsolutePath(),
                300
        );
        URL img = new URL("http://admin:admin123@193.226.12.217:8888/Streaming/Channels/1/picture");
        ImageCamera imageCamera = new ImageCamera("1", img);
        byte[] getImg = imageCamera.getSnapshot();

        ObjectFinderServiceImpl objectFinderService = new ObjectFinderServiceImpl();
        objectFinderService.bindImageClassifier(tensorflowMultibox);
        objectFinderService.bindObjectRecoginzer(tensorflowMultibox);
        objectFinderService.addCamera(imageCamera);
        List<Recognition> recognitions = objectFinderService.findObject(getImg);
        assertNotNull(recognitions);
    }
    
    @Test
    public void testBindRecognizers() {
        ObjectFinderServiceImpl objectFinderService = new ObjectFinderServiceImpl();
        objectFinderService.bindImageClassifier(new ObjectRecognizer() {
            @Override
            public List<Recognition> identifyImage(byte[] imageBytes) {
                return null;
            }

            @Override
            public List<Recognition> identifyImage(byte[] imageBytes, int inputSize) {
                return null;
            }
        });
        
        objectFinderService.bindObjectRecoginzer(new ObjectRecognizer() {
            @Override
            public List<Recognition> identifyImage(byte[] imageBytes) {
                return null;
            }

            @Override
            public List<Recognition> identifyImage(byte[] imageBytes, int inputSize) {
                return null;
            }
        });
    }

    @Test
    public void testDummyFunct() {
        ObjectFinderServiceImpl objectFinderService = new ObjectFinderServiceImpl();
        
        Recognition recognition = new Recognition() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public String getTitle() {
                return "DUMMY";
            }

            @Override
            public Float getConfidence() {
                return 1f;
            }

            @Override
            public byte[] getSource() {
                return new byte[0];
            }
        };
        
        objectFinderService.bindImageClassifier(new ObjectRecognizer() {
            @Override
            public List<Recognition> identifyImage(byte[] imageBytes) {
                return Arrays.asList(recognition);
            }

            @Override
            public List<Recognition> identifyImage(byte[] imageBytes, int inputSize) {
                return Arrays.asList(recognition);
            }
        });

        objectFinderService.bindObjectRecoginzer(new ObjectRecognizer() {
            @Override
            public List<Recognition> identifyImage(byte[] imageBytes) {
                return Arrays.asList(recognition);
            }

            @Override
            public List<Recognition> identifyImage(byte[] imageBytes, int inputSize) {
                return Arrays.asList(recognition);
            }
        });
        
        objectFinderService.addCamera(new MockCamera());
        
        List<Recognition> recognitions = objectFinderService.findObject(new byte[] {});
        assertEquals(1, recognitions.size());
        assertEquals("DUMMY", recognitions.get(0).getTitle());
    }
}
