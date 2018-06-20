import com.sma.core.object.finder.service.impl.ObjectFinderServiceImpl;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ObjectFinderTest {
    
    @Test
    public void testCameraAdd() {
        ObjectFinderServiceImpl objectFinderService = new ObjectFinderServiceImpl();
        objectFinderService.addCamera(new MockCamera());
    }
    
    @Test
    public void testBindRecognizers() {
        ObjectFinderServiceImpl objectFinderService = new ObjectFinderServiceImpl();
        objectFinderService.bindImageClassifier(new ObjectRecognizer() {
            @Override
            public List<Recognition> identifyImage(byte[] imageBytes) {
                return null;
            }
        });
        
        objectFinderService.bindObjectRecoginzer(new ObjectRecognizer() {
            @Override
            public List<Recognition> identifyImage(byte[] imageBytes) {
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
        });

        objectFinderService.bindObjectRecoginzer(new ObjectRecognizer() {
            @Override
            public List<Recognition> identifyImage(byte[] imageBytes) {
                return Arrays.asList(recognition);
            }
        });
        
        objectFinderService.addCamera(new MockCamera());
        
        List<Recognition> recognitions = objectFinderService.findObject(new byte[] {});
        assertEquals(1, recognitions.size());
        assertEquals("DUMMY", recognitions.get(0).getTitle());
    }
}
