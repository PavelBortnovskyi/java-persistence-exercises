package com.bobocode.dao;

import com.bobocode.exception.CompanyDaoException;
import com.bobocode.model.Company;
import com.bobocode.util.ExerciseNotCompletedException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.unwrap(Session.class).setDefaultReadOnly(true);
        entityManager.getTransaction().begin();
        try {
            TypedQuery<Company> findByIdQuery = entityManager.createQuery("select c from Company c join fetch c.products where c.id =: id", Company.class);
            findByIdQuery.setParameter("id", id);
            Company someCompany = findByIdQuery.getSingleResult();
            entityManager.getTransaction().commit();
            return someCompany;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new CompanyDaoException(String.format("Error during read operation by id: %d", id), e);
        } finally {
            entityManager.close();
        }
    }
}
