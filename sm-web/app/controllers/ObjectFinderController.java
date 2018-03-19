package controllers;

import com.sma.core.camera.api.Camera;
import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.ObjectFinderService;
import com.sma.object.finder.api.ObjectRecognizer;
import com.sma.object.finder.tf.TensorflowImageClassifier;
import models.User;
import models.dao.UserDAO;
import play.mvc.*;
import services.ImageUploadService;
import services.NetworkObjectFinderService;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ObjectFinderController extends Controller {
    
    private final ImageUploadService imageUploadService;
    
    private final NetworkObjectFinderService networkObjectFinderService;
    
    @Inject
    private UserDAO userDAO;
    
    @Inject
    public ObjectFinderController(ImageUploadService imageUploadService, NetworkObjectFinderService networkObjectFinderService) {
        this.imageUploadService = imageUploadService;
        this.networkObjectFinderService = networkObjectFinderService;
    }
    
    //@Security.Authenticated(Secured.class)
    public Result findObject() {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> picture = body.getFile("object");
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();

            try {
                List<Camera> cameras = new ArrayList<>();

                //TODO Demo only
                //TODO Retrieve cameras from user
                //URI fakeCameraImage = ObjectRecognizer.class.getResource("puppy_224.jpg").toURI();
                cameras.add(new ImageCamera("CAM1", new File("D:\\study\\lic\\sma-lic\\sm-core\\object-recognizer\\src\\main\\resources\\puppy_224.jpg")));
                
                User foundUser = userDAO.getUserByName(Http.Context.current().request().username());

                byte[] imageBytes = Files.readAllBytes(file.toPath());

                return ok(this.networkObjectFinderService.findObject(1L, cameras, imageBytes));
            } catch (IOException e) {
                return ok("No object found!");
            }
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }
}
