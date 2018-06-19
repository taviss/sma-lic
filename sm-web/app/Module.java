import com.google.inject.AbstractModule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;

import com.google.inject.Inject;
import com.sma.object.finder.tf.TensorflowObjectDetectionAPI;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.typesafe.config.Config;
import play.Configuration;
import play.Environment;
import services.*;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class Module extends AbstractModule {
    private static final int MB_INPUT_SIZE = 224;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";

    private Environment environment;
    private Configuration configuration;

    public Module(Environment environment, Configuration configuration){
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    public void configure() {
        // Use the system clock as the default implementation of Clock
        bind(Clock.class).toInstance(Clock.systemDefaultZone());
        // Ask Guice to create an instance of ApplicationTimer when the
        // application starts.
        bind(ApplicationTimer.class).asEagerSingleton();
        // Set AtomicCounter as the implementation for Counter.
        bind(Counter.class).to(AtomicCounter.class);
        bind(ImageUploadService.class).asEagerSingleton();

        try {
            ObjectRecognizer tensorflowMultibox = TensorflowObjectDetectionAPI.create(
                    configuration.getString("tfMultiboxModelPath"),
                    configuration.getString("tfMultiboxLocationPriors"),
                    224
            );
            bind(ObjectRecognizer.class).toInstance(tensorflowMultibox);
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        
        bind(NetworkObjectFinderService.class).asEagerSingleton();
    }

}
