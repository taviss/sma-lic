package services;

import com.typesafe.config.Config;
import models.Image;
import models.User;
import models.dao.ImageDAO;
import play.db.jpa.Transactional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Singleton
public class ImageUploadService {
    
    @Inject
    private Config config;
    
    @Transactional
    public boolean uploadImage(Image image, String fileName, String contentType, File file, User owner) {
        if (file != null && fileName != null) {
            String uploadPath = config.getString("uploadPath");

            String uploadedImagePath = uploadPath + owner.getId();
            new File(uploadedImagePath).mkdirs();
            
            uploadedImagePath += "/" + fileName;
            
            try {
                Files.copy(file.toPath(), new File(uploadedImagePath).toPath(), REPLACE_EXISTING);

                image.setImagePath(uploadedImagePath);
            } catch(IOException e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
