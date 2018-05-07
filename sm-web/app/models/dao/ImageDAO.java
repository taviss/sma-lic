package models.dao;

import models.Image;
import models.User;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
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

    public Image getImageByClassAndOwner(User user, String cls) {
        return wrap(em -> getImageByClassAndOwner(em, user, cls));
    }

    public Image getImageByClassAndOwner(EntityManager em, User user, String cls) {
        CriteriaQuery<Image> criteriaQuery = em.getCriteriaBuilder().createQuery(Image.class);
        Root<Image> root = criteriaQuery.from(Image.class);

        criteriaQuery.select(root);
        Predicate userP = em.getCriteriaBuilder().equal(root.get("owner_id"), user.getId());
        Predicate classP = em.getCriteriaBuilder().equal(root.get("i_class"), cls);

        criteriaQuery.where(userP).where(classP);
        Query query = em.createQuery(criteriaQuery);
        @SuppressWarnings("unchecked")
        List<Image> foundImages = (List<Image>) query.getResultList();

        if (foundImages.isEmpty()) return null;
        else if (foundImages.size() == 1) return foundImages.get(0);
            //TBA: throw new exception
        else return null;

        //TBA
    }

    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }
}
