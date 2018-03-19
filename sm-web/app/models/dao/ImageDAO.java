package models.dao;

import models.Image;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.function.Function;

public class ImageDAO {
    private final JPAApi jpaApi;

    @Inject
    public ImageDAO(JPAApi api) {
        this.jpaApi = api;
    }

    public Image create(Image image) {
        return wrap(em -> create(em, image));
    }

    public Image update(Image image) {
        return wrap(em -> update(em, image));
    }

    public void delete(Image image) {
        //FIXME
        jpaApi.withTransaction(new Runnable() {
            @Override
            public void run() {
                delete(jpaApi.em(), image);
            }
        });
    }

    /**
     * Returns the Image with "id"
     * @param id
     * @return User
     */
    public Image get(Long id) {
        return wrap(em -> get(em, id));
    }


    public Image create(EntityManager em, Image image) {
        image.setId(null);
        em.persist(image);
        return image;
    }

    public Image update(EntityManager em, Image image) {
        return em.merge(image);
    }

    public void delete(EntityManager em, Image image) {
        em.remove(image);
    }

    /**
     * Returns the Image with "id"
     * @param id
     * @return User
     */
    public Image get(EntityManager em, Long id) {
        return em.find(Image.class, id);
    }

    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }
}
