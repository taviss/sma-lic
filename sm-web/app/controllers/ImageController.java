package controllers;

import com.typesafe.config.Config;
import models.Image;
import models.User;
import models.dao.ImageDAO;
import models.dao.UserDAO;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.ImageUploadService;

import javax.inject.Inject;
import java.io.File;

public class ImageController extends Controller {
    @Inject
    private FormFactory formFactory;
    
    @Inject
    private UserDAO userDAO;
    
    private final ImageUploadService imageUploadService;
    
    @Inject
    public ImageController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }
    
    @Security.Authenticated
    @Transactional
    public Result uploadImage() {
        Form<Image> imageForm = formFactory.form(Image.class).bindFromRequest();
        
        if(imageForm.hasErrors()) {
            return badRequest(imageForm.errorsAsJson());
        }
        
        Image uploadedImage = imageForm.get();
        
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> imageFile = body.getFile("object");
        
        if (imageFile != null) {
            User foundUser = userDAO.getUserByName(Http.Context.current().request().username());
            
            boolean uploadSuccess = imageUploadService.uploadImage(uploadedImage, imageFile.getFilename(), imageFile.getContentType(), imageFile.getFile(), foundUser);

            if(uploadSuccess) {
                return ok("File uploaded");
            } else {
                return badRequest("Error while uploading");
            }
        } else {
            return badRequest("Null image");
        }
        
    }
    
}
