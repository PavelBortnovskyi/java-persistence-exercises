package com.bobocode.dao;

import com.bobocode.model.Account;
import com.bobocode.model.Photo;
import com.bobocode.model.PhotoComment;
import com.bobocode.util.ExerciseNotCompletedException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        performWithinPersistenceContext(entityManager -> entityManager.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return performReturningWithinPersistenceContext(entityManager -> entityManager.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        return performReturningWithinPersistenceContext(entityManager ->
                entityManager.createQuery("select p from Photo p", Photo.class).getResultList()
        );
    }

    @Override
    public void remove(Photo photo) {
        performWithinPersistenceContext(entityManager -> {
            Photo mergedPhoto = entityManager.merge(photo);
            entityManager.remove(mergedPhoto);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        performWithinPersistenceContext(entityManager -> {
            Photo photo = entityManager.find(Photo.class, photoId);
            PhotoComment photoComment = new PhotoComment();
            photoComment.setText(comment);
            photoComment.setPhoto(photo);
            photo.addComment(photoComment);
            entityManager.merge(photo);
        });
    }

    private void performWithinPersistenceContext(Consumer<EntityManager> operation) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            operation.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            //throw new AccountDaoException(String.format("Error in operation: %s", operation.toString()), e);
        } finally {
            entityManager.close();
        }
    }

    private <T> T performReturningWithinPersistenceContext(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        T result;

        try {
            result = entityManagerFunction.apply(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            return null;
            //throw new AccountDaoException("Error performing dao returning operation. Transaction is rolled back!", e);
        } finally {
            entityManager.close();
        }
        return result;
    }
}
