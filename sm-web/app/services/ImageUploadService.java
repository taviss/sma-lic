package services;

import com.typesafe.config.Config;
import models.Image;
import models.User;
import models.dao.ImageDAO;
import play.db.jpa.Transactional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public class ImageUploadService {
    
    @Inject
    private ImageDAO imageDAO;
    
    @Inject
    private Config config;
    
    @Transactional
    public boolean uploadImage(Image image, String fileName, String contentType, File file, User owner) {
        if (file != null && fileName != null && contentType != null) {
            String uploadPath = config.getString("uploadPath");

            String uploadedImagePath = uploadPath + owner.getId() + File.separator + fileName;
            file.renameTo(new File(uploadedImagePath));

            image.setImagePath(uploadedImagePath);
            imageDAO.create(image);

            return true;
        } else {
            return false;
        }
    }
}
