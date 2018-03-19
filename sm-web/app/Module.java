import com.google.inject.AbstractModule;

import java.io.File;
import java.net.URI;
import java.time.Clock;

import com.google.inject.Inject;
import com.sma.object.finder.api.ObjectRecognizer;
import com.sma.object.finder.tf.TensorflowImageClassifier;
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

        URI tfModel = new File(configuration.getString("tfModelPath")).toURI();
        URI tfLabels = new File(configuration.getString("tfLabelsPath")).toURI();
        TensorflowImageClassifier tensorflowImageClassifier = new TensorflowImageClassifier(tfModel, tfLabels);
        bind(ObjectRecognizer.class).toInstance(tensorflowImageClassifier);
        
        bind(NetworkObjectFinderService.class).asEagerSingleton();
    }

}
