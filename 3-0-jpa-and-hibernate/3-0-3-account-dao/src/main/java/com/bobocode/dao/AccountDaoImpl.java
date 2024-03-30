package com.bobocode.dao;

import com.bobocode.exception.AccountDaoException;
import com.bobocode.model.Account;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        performWithinPersistenceContext(entityManager -> entityManager.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return performReturningWithinPersistenceContext(entityManager -> entityManager.find(Account.class, id));
    }

    @Override
    public Account findByEmail(String email) {
        return performReturningWithinPersistenceContext(entityManager -> {
            TypedQuery<Account> findByEmailQuery = entityManager.createQuery("select a from Account a where a.email = :email", Account.class);
            findByEmailQuery.setParameter("email", email);
            return findByEmailQuery.getSingleResult();
        });
    }

    private Account getAccount(String email, EntityManager entityManager) {
        TypedQuery<Account> findByEmailQuery = entityManager.createQuery("select a from Account a where a.email = :email", Account.class);
        findByEmailQuery.setParameter("email", email);
        return findByEmailQuery.getSingleResult();
    }

    @Override
    public List<Account> findAll() {
        return performReturningWithinPersistenceContext(entityManager ->
                entityManager.createQuery("select a from Account a", Account.class).getResultList());
    }

    @Override
    public void update(Account account) {
        performWithinPersistenceContext(entityManager -> entityManager.merge(account));
    }

    @Override
    public void remove(Account account) {
        performWithinPersistenceContext(entityManager -> {
            Account mergedAccount = entityManager.merge(account);
            entityManager.remove(mergedAccount);
        });
    }

    private void performWithinPersistenceContext(Consumer<EntityManager> operation) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            operation.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException(String.format("Error in operation: %s", operation.toString()), e);
        } finally {
            entityManager.close();
        }
    }

    private <T> T performReturningWithinPersistenceContext(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        T result;

        try {
            result = entityManagerFunction.apply(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Error performing dao returning operation. Transaction is rolled back!", e);
        } finally {
            entityManager.close();
        }
        return result;
    }
}

