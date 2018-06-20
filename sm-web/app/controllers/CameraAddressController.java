package controllers;

import com.google.inject.Inject;
import models.CameraAddress;
import models.User;
import models.dao.CameraAddressDAO;
import models.dao.UserDAO;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.camera;

import javax.transaction.Transactional;

import static utils.PasswordHashing.hashPassword;

public class CameraAddressController extends Controller {
    @Inject
    private FormFactory formFactory;

    @Inject
    private CameraAddressDAO cameraAddressDAO;

    @Inject
    private UserDAO userDAO;

    @Security.Authenticated(Secured.class)
    public Result createCameraForm() {
        return ok(camera.render());
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result createCamera() {
        Form<CameraAddress> form = formFactory.form(CameraAddress.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }

        CameraAddress cameraAddress = form.get();
        
        User foundUser = userDAO.getUserByName(Http.Context.current().request().username());
        
        cameraAddress.setOwner(foundUser);
        //TODO How to handle this -> needed in plaintext for connection to camera
        //cameraAddress.setPassword(hashPassword(cameraAddress.getPassword().toCharArray()));
        CameraAddress cameraAddressRes = cameraAddressDAO.create(cameraAddress);
        return ok(Json.toJson(cameraAddressRes));
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result deleteCamera(Long id) {
        CameraAddress cameraAddress = cameraAddressDAO.get(id);
        if (cameraAddress == null) {
            return notFound("No such camera");
        } else {
            User foundUser = userDAO.getUserByName(Http.Context.current().request().username());
            if(cameraAddress.getOwner().getId().equals(foundUser.getId())) {
                cameraAddressDAO.delete(cameraAddress.getId());
                return ok("Product deleted: " + cameraAddress.getAddress());
            } else {
                return forbidden("You're not the owner of this camera!");
            }
            
        }
    }
}
