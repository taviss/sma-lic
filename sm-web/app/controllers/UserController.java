package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import forms.LoginForm;
import forms.PasswordChangeForm;
import forms.PasswordResetForm;
import models.User;
import models.dao.UserDAO;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.*;
import services.Mailer;
import utils.PasswordHashing;
import views.html.index;
import views.html.login;
import views.html.password;
import views.html.user;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static utils.PasswordHashing.hashPassword;
import static utils.PasswordHashing.validatePassword;

/**
 * Created by octavian.salcianu on 8/29/2016.
 */
public class UserController extends Controller {

    @Inject
    private FormFactory formFactory;

    @Inject
    private UserDAO userDAO;
    
    @Inject
    private Mailer mailerClient;

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result adminPanel() {
        return ok(index.render());
    }
    
    @Transactional
    public Result createUser() {
        Form<User> form = formFactory.form(User.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        
        User foundUser = userDAO.getUserByName(form.get().getUserName());

        if (foundUser != null) {
            Map<String, List<String>> allMessages = new HashMap<>();
            allMessages.put("userName", Arrays.asList("Username in use!"));
            return badRequest(Json.toJson(allMessages));
        } else {
            User foundMail = userDAO.getUserByMail(form.get().getUserMail());
            if(foundMail != null) {
                Map<String, List<String>> allMessages = new HashMap<>();
                allMessages.put("userMail", Arrays.asList("Email in use!"));
                return badRequest(Json.toJson(allMessages));
            } else {
                User createdUser = form.get();
                createdUser.setUserPass(hashPassword(createdUser.getUserPass().toCharArray()));
                createdUser.setUserToken(UUID.randomUUID().toString());
                createdUser.setUserActive(true);
                userDAO.create(createdUser);
                return ok("Success");
            }
        }
    }
    
    @Transactional
    public Result createUserForm() {
        return ok(user.render());
    }

    @Transactional
    public Result login() {
        return ok(login.render());
    }

    @Transactional
    public Result isLogged() {
        if(session().get("user") != null) {
            return ok(session().get("user"));
        } else {
            return ok();
        }
    }

    @Transactional(readOnly = true)
    public Result tryLogin() {
        Form<LoginForm> form = formFactory.form(LoginForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }

        User foundUser = userDAO.getUserByName(form.get().userName);

        if (foundUser == null) {
            form.reject("userName", "This account does not exist");
            return badRequest(form.errorsAsJson());
        } else {
            try {
                if (!validatePassword(form.get().userPass.toCharArray(), foundUser.getUserPass())) {
                    form.reject("userPass", "Wrong password");
                    return badRequest(form.errorsAsJson());
                }
            } catch (NoSuchAlgorithmException |InvalidKeySpecException e) {
                form.reject("userName", "Internal error");
                return badRequest(form.errorsAsJson());
            }
        }

        session().clear();
        session("user", form.get().userName);
        String remote = request().remoteAddress();
        Logger.info("User logged in: " + form.get().userName + " (" + remote + ")");
        return ok();
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result logoutUser() {
        String remote = request().remoteAddress();
        Logger.info("User logged out: " + session().get("user") + " (" + remote + ")");
        session().clear();
        return ok();
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result changeUserPassword() {
        Form<PasswordChangeForm> form = formFactory.form(PasswordChangeForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return ok(form.errorsAsJson());
        }

        User foundUser = userDAO.getUserByName(Http.Context.current().request().username());

        foundUser.setUserPass(hashPassword(form.get().newPassword.toCharArray()));
        userDAO.update(foundUser);
        String remote = request().remoteAddress();
        Logger.info("Changed password: " + foundUser.getUserName() + " (" + remote + ")");
        return ok();
    }

    @Transactional
    public Result changeUserPasswordForm() {
        return(ok(password.render()));
    }

    /**
     * Send a token by email if userName and userMail match. The token can then be used to reset password
     * @return Result
     * @throws EmailException
     * @throws MalformedURLException
     */
    @Transactional
    @BodyParser.Of(value = BodyParser.Json.class)
    public Result resetUserPassword() throws EmailException, MalformedURLException {
        JsonNode json = request().body().asJson();
        Form<PasswordResetForm> form = formFactory.form(PasswordResetForm.class).bind(json);

        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }

        User foundUser = userDAO.getUserByName(form.get().userName);

        try {
            foundUser.setUserToken(UUID.randomUUID().toString());
            userDAO.update(foundUser);
            mailerClient.sendPasswordResetMail(foundUser);
            String remote = request().remoteAddress();
            Logger.info("Password reset request: " + form.get().userName + "(" + remote + ")");
            return ok("Password reset request sent");
        } catch (NullPointerException e) {
            String remote = request().remoteAddress();
            Logger.info("Password reset attempt: " + form.get().userName + "(" + remote + ")");
            return badRequest("User does not exist");
        }
    }

    /**
     * Takes a token and sends the coresponding user a new random password by email. Resets the token afterwards
     * @param token
     * @return Result
     * @throws EmailException
     * @throws MalformedURLException
     */
    @Transactional
    public Result confirmPasswordReset(String token) throws EmailException, MalformedURLException {
        User foundUser = userDAO.getUserByToken(token);
        if (foundUser == null) {
            return badRequest("Invalid token");
        } else {
            //Set the password in plain text for the email sending
            foundUser.setUserPass(PasswordHashing.getRandomString());
            mailerClient.sendRandomPasswordMail(foundUser);
            //Now hash the password and save it
            foundUser.setUserToken(UUID.randomUUID().toString());
            foundUser.setUserPass(hashPassword(foundUser.getUserPass().toCharArray()));
            userDAO.update(foundUser);
            return ok("Password sent via email");
        }
    }
}
