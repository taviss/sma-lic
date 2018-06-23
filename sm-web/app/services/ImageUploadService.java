package services;

import com.typesafe.config.Config;
import models.Image;
import models.User;
import models.dao.ImageDAO;
import play.db.jpa.Transactional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Service for uploading an image to the server
 */
@Singleton
public class ImageUploadService {
    
    @Inject
    private Config config;

    /**
     * Uploads an image to the server and sets main path
     * @param image
     * @param fileName
     * @param contentType
     * @param file
     * @param owner
     * @return
     */
    @Transactional
    public boolean uploadImage(Image image, String fileName, String contentType, File file, User owner) {
        if (file != null && fileName != null) {
            String uploadPath = config.getString("uploadPath");

            String uploadedImagePath = uploadPath + owner.getId();
            new File(uploadedImagePath).mkdirs();
            
            uploadedImagePath += "/" + image.getId() + ".jpg";
            System.out.println("Uploading image " + image.getId() + " to " + uploadedImagePath);
            
            try {
                Files.copy(file.toPath(), new File(uploadedImagePath).toPath(), REPLACE_EXISTING);

                image.setImagePath(uploadedImagePath);
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Uploads an image to the server and sets last seen path
     * @param image
     * @param fileName
     * @param newImage
     * @param owner
     * @return
     */
    @Transactional
    public boolean uploadLastSeenImage(Image image, String fileName, byte[] newImage, User owner) {
        if (newImage != null && fileName != null) {
            String uploadPath = config.getString("uploadPath");

            String uploadedImagePath = uploadPath + owner.getId() + "/last";
            new File(uploadedImagePath).mkdirs();

            uploadedImagePath += "/" + image.getId() + ".jpg";

            System.out.println("Uploading last seen for image " + image.getId() + " to " + uploadedImagePath);

            try {
                new File(uploadedImagePath).delete();
                FileOutputStream fos = new FileOutputStream(uploadedImagePath);
                try {
                    fos.write(newImage);
                }
                finally {
                    fos.close();
                }
                image.setLastSeenImage(uploadedImagePath);
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
