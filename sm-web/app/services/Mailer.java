package services;

import models.User;
import play.Configuration;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import org.apache.commons.mail.EmailException;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;


public class Mailer {
    @Inject
    private MailerClient mailerClient;

    private final play.i18n.MessagesApi messagesApi;

    @Inject
    Mailer(MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
    }

    /**
     * Sends the newly registered user a confirmation mail containing a token that can be used to activate the account
     * @param user
     * @throws EmailException
     * @throws MalformedURLException
     */
    public void sendConfirmationMail(User user) throws EmailException, MalformedURLException {
        Collection<Lang> candidates = Collections.singletonList(new Lang(Locale.US));
        Messages messages = messagesApi.preferred(candidates);
        //Create the strings
        String subject = messages.at("mail.confirmation.subject");
        String urlString = "http://" + Configuration.root().getString("server.hostname") + "/confirm/" + user.getUserToken();
        URL url = new URL(urlString);
        String message = messages.at("mail.confirmation.body") + ", " + url.toString();

        //Compose the email
        Email email = new Email()
                .setSubject(subject)
                .setFrom("test@gmail.com")
                .addTo(user.getUserMail())
                .setBodyText(message);

        //Send the email
        mailerClient.send(email);
    }

    /**
     * Sends the user an email containing a token that can be used to reset their password
     * @param user
     * @throws EmailException
     * @throws MalformedURLException
     */
    public void sendPasswordResetMail(User user) throws EmailException, MalformedURLException {
        Collection<Lang> candidates = Collections.singletonList(new Lang(Locale.US));
        Messages messages = messagesApi.preferred(candidates);
        //Create the strings
        String subject = messages.at("mail.password.reset.subject");
        String urlString = "http://" + Configuration.root().getString("server.hostname") + "/confirm/reset/" + user.getUserToken();
        URL url = new URL(urlString);
        String message = messages.at("mail.password.reset.body") + ", " + url.toString();

        //Compose the email
        Email email = new Email()
                .setSubject(subject)
                .setFrom("test@gmail.com")
                .addTo(user.getUserMail())
                .setBodyText(message);

        //Send the email
        mailerClient.send(email);
    }

    /**
     * Sends the user an email containing the random password generated upon validating the token
     * @param user
     * @throws EmailException
     * @throws MalformedURLException
     */
    public void sendRandomPasswordMail(User user) throws EmailException, MalformedURLException {
        Collection<Lang> candidates = Collections.singletonList(new Lang(Locale.US));
        Messages messages = messagesApi.preferred(candidates);
        //Create the strings
        String subject = messages.at("mail.password.random.subject");
        String message = messages.at("mail.password.random.body") + user.getUserPass();

        //Compose the email
        Email email = new Email()
                .setSubject(subject)
                .setFrom("test@gmail.com")
                .addTo(user.getUserMail())
                .setBodyText(message);

        //Send the email
        mailerClient.send(email);
    }
}