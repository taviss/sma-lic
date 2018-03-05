package forms;

import models.User;
import models.dao.UserDAO;
import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Http;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static utils.PasswordHashing.validatePassword;

/**
 * Created by octavian.salcianu on 9/1/2016.
 */
public class PasswordChangeForm {

    @Constraints.Required()
    @Constraints.MinLength(6)
    @Constraints.MaxLength(256)
    public String oldPassword;

    @Constraints.Required()
    @Constraints.MinLength(6)
    @Constraints.MaxLength(256)
    public String newPassword;

    @Constraints.Required()
    @Constraints.MinLength(6)
    @Constraints.MaxLength(256)
    public String newPasswordRepeat;
    
    private UserDAO userDAO;
    
    @Inject
    public PasswordChangeForm(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<ValidationError> validate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        
        User foundUser = userDAO.getUserByName(Http.Context.current().request().username());

        //Check if password repeat is the same as password
        if (!newPassword.equals(newPasswordRepeat)) {
            errors.add(new ValidationError("newPasswordRepeat", "Password repeat does not match new password"));
        }

        if(foundUser == null) {
            String remote = Http.Context.current().request().remoteAddress();
            Logger.info("User tried to change password without being logged:" + remote);
            errors.add(new ValidationError("oldPassword", "Bad password"));
        } else {
            if (!validatePassword(oldPassword.toCharArray(), foundUser.getUserPass())) {
                String remote = Http.Context.current().request().remoteAddress();
                Logger.info("Change password attempt: " + foundUser.getUserName() + " (" + remote + ")");
                errors.add(new ValidationError("oldPassword", "Bad password"));
            }
        }

        return errors.isEmpty() ? null : errors;
    }
}

