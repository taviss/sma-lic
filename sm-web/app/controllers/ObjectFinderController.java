package controllers;

import com.sma.core.camera.api.Camera;
import com.sma.core.camera.st.impl.ImageCamera;
import models.User;
import models.dao.UserDAO;
import play.mvc.*;
import services.ImageUploadService;
import services.NetworkObjectFinderService;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
                cameras.add(new ImageCamera("CAM2", new File("D:\\study\\lic\\sma-lic\\sm-core\\object-recognizer\\src\\main\\resources\\puppy_224_mod.jpg")));
                
                User foundUser = userDAO.getUserByName(Http.Context.current().request().username());

                BufferedImage bufferedImage = ImageIO.read(file);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                
                List<byte[]> recognizedImages = this.networkObjectFinderService.findObject(1L, cameras, imageBytes);

                if(recognizedImages != null && recognizedImages.size() > 0) {
                    System.out.println("Total recognitions: " + recognizedImages.size());
                    return ok(recognizedImages.get(0)).as("image/jpg");
                } else {
                    return ok("Object not found!");
                }
            } catch (IOException e) {
                return ok("No object found!");
            }
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }
}
