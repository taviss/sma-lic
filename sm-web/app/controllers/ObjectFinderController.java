package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sma.core.camera.api.Camera;
import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.api.ObjectFinderService;
import com.sma.core.object.finder.service.impl.ObjectFinderServiceImpl;
import com.sma.object.recognizer.api.ObjectRecognizer;
import com.sma.object.recognizer.api.Recognition;
import com.sma.recognition.interpreter.impl.AbstractRecognitionInterpreter;
import models.CameraAddress;
import models.Image;
import models.User;
import models.dao.CameraAddressDAO;
import models.dao.ImageDAO;
import models.dao.UserDAO;
import play.db.jpa.Transactional;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for object finding related operations
 */
public class ObjectFinderController extends Controller {
    /**
     * The service for uploading an image
     */
    private final ImageUploadService imageUploadService;

    /**
     * The service for finding an object
     */
    private final NetworkObjectFinderService networkObjectFinderService;

    /**
     * The object recognizer
     */
    private final ObjectRecognizer objectRecognizer;
    
    @Inject
    private UserDAO userDAO;
    
    @Inject
    private CameraAddressDAO cameraAddressDAO;
    
    @Inject
    private ImageDAO imageDAO;
    
    @Inject
    public ObjectFinderController(ImageUploadService imageUploadService, 
            NetworkObjectFinderService networkObjectFinderService, 
            ObjectRecognizer objectRecognizer) 
    {
        this.imageUploadService = imageUploadService;
        this.networkObjectFinderService = networkObjectFinderService;
        this.objectRecognizer = objectRecognizer;
    }
    
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result findObject() {
        JsonNode json = request().body().asJson();

        if (json != null) {
            long id = json.get("id").asLong();
            String name = json.get("name").textValue();

            try {
                List<Camera> cameras = new ArrayList<>();

                //TODO Demo only
                //TODO Retrieve cameras from user
                //URI fakeCameraImage = ObjectRecognizer.class.getResource("puppy_224.jpg").toURI();
                //cameras.add(new ImageCamera("CAM1", new File("D:\\study\\lic\\sma-lic\\sm-core\\object-recognizer.tf\\src\\main\\resources\\puppies_224.jpg")));
                //cameras.add(new ImageCamera("CAM2", new File("D:\\study\\lic\\sma-lic\\sm-core\\object-recognizer.tf\\src\\main\\resources\\puppy_224.jpg")));
                //cameras.add(new ImageCamera("CAM3", new File("D:\\tf_demo\\room.jpg")));
                
                User foundUser = userDAO.getUserByName(Http.Context.current().request().username());
                
                for(CameraAddress cameraAddress : foundUser.getCameraAddresses()) {
                    Camera camera = CameraFactory.createCamera(cameraAddress);
                    if(camera != null) {
                        System.out.println("Added camera " + camera.getId() + " of " + camera);
                        cameras.add(camera);
                    }
                }
                
                Image image = imageDAO.get(id);
                if(image != null) {
                    File img = new File(image.getImagePath());
                    byte[] imageBytes = Files.readAllBytes(img.toPath());
                    List<Recognition> recognizedImages = this.networkObjectFinderService.findObject(foundUser.getId(), cameras, imageBytes);
                    if (recognizedImages != null && recognizedImages.size() > 0) {
                        System.out.println("Total recognitions: " + recognizedImages.size());
                        imageUploadService.uploadLastSeenImage(image, String.valueOf(image.getId()), recognizedImages.get(0).getSource(), foundUser);
                        imageDAO.update(image);
                        //(recognizedImages.get(0).getSource()).as("image/jpg"
                        return ok(Json.toJson(recognizedImages));
                    } else {
                        if(image.getLastSeenImage() != null && !image.getLastSeenImage().trim().equals("")) {
                            File lastSeen = new File(image.getLastSeenImage());
                            return ok(Files.readAllBytes(lastSeen.toPath())).as("image/jpg");
                        }
                        return ok("Object not found!");
                    }
                } else {
                    System.out.println("No such image in db");
                    return ok("Object not found!");
                }
            } catch (IOException e) {
                e.printStackTrace();
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
                recognitions = recognitions.stream().filter(p -> p.getConfidence() >= AbstractRecognitionInterpreter.MINIMUM_CONFIDENCE).collect(Collectors.toList());
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
