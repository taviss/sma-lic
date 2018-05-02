package controllers;

import com.sma.core.camera.api.Camera;
import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.api.ObjectFinderService;
import com.sma.core.object.finder.service.impl.ObjectFinderServiceImpl;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import models.CameraAddress;
import models.User;
import models.dao.CameraAddressDAO;
import models.dao.UserDAO;
import play.libs.Json;
import play.mvc.*;
import services.ImageUploadService;
import services.NetworkObjectFinderService;
import utils.CameraFactory;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectFinderController extends Controller {
    
    private final ImageUploadService imageUploadService;
    
    private final NetworkObjectFinderService networkObjectFinderService;
    
    private final ObjectRecognizer objectRecognizer;
    
    @Inject
    private UserDAO userDAO;
    
    @Inject
    private CameraAddressDAO cameraAddressDAO;
    
    @Inject
    public ObjectFinderController(ImageUploadService imageUploadService, NetworkObjectFinderService networkObjectFinderService, ObjectRecognizer objectRecognizer) {
        this.imageUploadService = imageUploadService;
        this.networkObjectFinderService = networkObjectFinderService;
        this.objectRecognizer = objectRecognizer;
    }
    
    @Security.Authenticated(Secured.class)
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
                //cameras.add(new ImageCamera("CAM1", new File("D:\\study\\lic\\sma-lic\\sm-core\\object-recognizer\\src\\main\\resources\\puppies_224.jpg")));
                //cameras.add(new ImageCamera("CAM2", new File("D:\\study\\lic\\sma-lic\\sm-core\\object-recognizer\\src\\main\\resources\\puppy_224.jpg")));
                
                User foundUser = userDAO.getUserByName(Http.Context.current().request().username());
                
                for(CameraAddress cameraAddress : foundUser.getCameraAddresses()) {
                    cameras.add(CameraFactory.createCamera(cameraAddress));
                }
                

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
                return ok("Object not found!");
            }
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }
    
    @Security.Authenticated(Secured.class)
    public Result recognizeObject() {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> picture = body.getFile("object");
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();
            
            try {

                BufferedImage bufferedImage = ImageIO.read(file);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                List<Recognition> recognitions = this.objectRecognizer.identifyImage(imageBytes);
                recognitions = recognitions.stream().filter(p -> p.getConfidence() > 0.7f).collect(Collectors.toList());
                //FIXME
                recognitions.sort(new ObjectFinderServiceImpl.ConfidenceComparator().reversed());
                
                
                return ok(Json.toJson(recognitions));
            } catch (IOException e) {
                return ok("No object found!");
            }
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }
}
