package controllers;

import akka.util.ByteString;
import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.ObjectFinderService;
import com.sma.object.finder.api.ObjectRecognizer;
import com.sma.object.finder.tf.TensorflowImageClassifier;
import play.mvc.*;
import play.mvc.Security;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class ObjectFinderController extends Controller {
    //@Security.Authenticated(Secured.class)
    public Result findObject() {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> picture = body.getFile("object");
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();
            
            
            try {
                ObjectFinderService objectFinderService = new ObjectFinderService();
                
                URI fakeCameraImage = ObjectRecognizer.class.getResource("puppy_224.jpg").toURI();
                objectFinderService.addCamera(new ImageCamera("CAM1", new File(fakeCameraImage)));

                URI tfModel = ObjectFinderService.class.getClassLoader().getResource("tensorflow_inception_graph.pb").toURI();
                URI tfLabels = ObjectFinderService.class.getClassLoader().getResource("imagenet_comp_graph_label_strings.txt").toURI();
                objectFinderService.bindObjectRecoginzer(new TensorflowImageClassifier(tfModel, tfLabels));
                
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                
                objectFinderService.findObject(imageBytes);
            } catch(IOException|URISyntaxException e) {
                return ok("No object found!");
            }
            
            return ok("File uploaded");
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }
}
