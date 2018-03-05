package models.dao;

import models.User;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.function.Function;

/**
 * Created by octavian.salcianu on 8/29/2016.
 */
public class UserDAO {
    private final JPAApi jpaApi;

    @Inject
    public UserDAO(JPAApi api) {
        this.jpaApi = api;
    }
    
    public User create(User user) {
        return wrap(em -> create(em, user));
    }
    
    public User update(User user) {
        return wrap(em -> update(em, user));
    }
    
    public void delete(User user) {
        //FIXME
        jpaApi.withTransaction(new Runnable() {
            @Override
            public void run() {
                delete(jpaApi.em(), user);
            }
        });
    }

    /**
     * Returns the user with "id"
     * @param id
     * @return User
     */
    public User get(Long id) {
        return wrap(em -> get(em, id));
    }

    /**
     * Returns the user with specific username or null if it doesn't exist
     * @param userName
     * @return
     */
    public User getUserByName(String userName) {
        return wrap(em -> getUserByName(em, userName));
    }

    /**
     * Returns the user with specific email or null if it doesn't exist
     * @param userMail
     * @return
     */
    public User getUserByMail(String userMail) {
        return wrap(em -> getUserByMail(em, userMail));
    }

    /**
     * Returns the user with specific token or null if it doesn't exist
     * @param userToken
     * @return
     */
    public User getUserByToken(String userToken) {
        return wrap(em -> getUserByToken(em, userToken));
    }

    /**
     * Adds the newly created user to the database(luckily) and returns the new user with the new "id" from db
     * @param user : User
     * @return user : User
     */
    public User create(EntityManager em, User user) {
        user.setId(null);
        em.persist(user);
        return user;
    }

    public User update(EntityManager em, User user) {
        return em.merge(user);
    }

    public void delete(EntityManager em, User user) {
        em.remove(user);
    }

    /**
     * Returns the user with "id"
     * @param id
     * @return User
     */
    public User get(EntityManager em, Long id) {
        return em.find(User.class, id);
    }

    /**
     * Returns the user with specific username or null if it doesn't exist
     * @param userName
     * @return
     */
    public User getUserByName(EntityManager em, String userName) {
        CriteriaQuery<User> criteriaQuery = em.getCriteriaBuilder().createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(root);
        Predicate userNameP = em.getCriteriaBuilder().equal(root.get("userName"), userName);

        criteriaQuery.where(userNameP);
        Query query = em.createQuery(criteriaQuery);
        @SuppressWarnings("unchecked")
        List<User> foundUsers = (List<User>) query.getResultList();

        if (foundUsers.isEmpty()) return null;
        else if (foundUsers.size() == 1) return foundUsers.get(0);
            //TBA: throw new exception
        else return null;

        //TBA
    }

    /**
     * Returns the user with specific email or null if it doesn't exist
     * @param userMail
     * @return
     */
    public User getUserByMail(EntityManager em, String userMail) {
        CriteriaQuery<User> criteriaQuery = em.getCriteriaBuilder().createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(root);
        Predicate userMailP = em.getCriteriaBuilder().equal(root.get("userMail"), userMail);

        criteriaQuery.where(userMailP);
        Query query = em.createQuery(criteriaQuery);
        @SuppressWarnings("unchecked")
        List<User> foundUsers = (List<User>) query.getResultList();

        if (foundUsers.isEmpty()) return null;
        else if (foundUsers.size() == 1) return foundUsers.get(0);
            //TBA: throw new exception
        else return null;

        //TBA
    }

    /**
     * Returns the user with specific token or null if it doesn't exist
     * @param userToken
     * @return
     */
    public User getUserByToken(EntityManager em, String userToken) {
        CriteriaQuery<User> criteriaQuery = em.getCriteriaBuilder().createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(root);
        Predicate userTokenP = em.getCriteriaBuilder().equal(root.get("userToken"), userToken);

        criteriaQuery.where(userTokenP);
        Query query = em.createQuery(criteriaQuery);
        @SuppressWarnings("unchecked")
        List<User> foundUsers = (List<User>) query.getResultList();
        if (foundUsers.isEmpty()) return null;
        else if (foundUsers.size() == 1) return foundUsers.get(0);
            //TBA: throw new exception
        else return null;
    }

    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }
}
