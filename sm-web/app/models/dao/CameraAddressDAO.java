package models.dao;

import models.CameraAddress;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.function.Function;

public class CameraAddressDAO {
    private final JPAApi jpaApi;

    @Inject
    public CameraAddressDAO(JPAApi api) {
        this.jpaApi = api;
    }

    public CameraAddress create(CameraAddress cameraAddress) {
        return wrap(em -> create(em, cameraAddress));
    }

    public CameraAddress update(CameraAddress cameraAddress) {
        return wrap(em -> update(em, cameraAddress));
    }

    public void delete(CameraAddress cameraAddress) {
        //FIXME
        jpaApi.withTransaction(new Runnable() {
            @Override
            public void run() {
                delete(jpaApi.em(), cameraAddress);
            }
        });
    }

    /**
     * Returns the cameraAddress with "id"
     * @param id
     * @return User
     */
    public CameraAddress get(Long id) {
        return wrap(em -> get(em, id));
    }

    /**
     * Adds the newly created cameraAddress to the database(luckily) and returns the new cameraAddress with the new "id" from db
     * @param user : User
     * @return user : User
     */
    public CameraAddress create(EntityManager em, CameraAddress cameraAddress) {
        cameraAddress.setId(null);
        em.persist(cameraAddress);
        return cameraAddress;
    }

    public CameraAddress update(EntityManager em, CameraAddress cameraAddress) {
        return em.merge(cameraAddress);
    }

    public void delete(EntityManager em, CameraAddress cameraAddress) {
        em.remove(cameraAddress);
    }

    /**
     * Returns the cameraAddress with "id"
     * @param id
     * @return User
     */
    public CameraAddress get(EntityManager em, Long id) {
        return em.find(CameraAddress.class, id);
    }

    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }
}
