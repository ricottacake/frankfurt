package com.aceliq.frankfurt.database;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.aceliq.frankfurt.models.User;

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Throwable.class})
public class UserDaoImpl implements UserDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private ApplicationContext context;

  @Override
  public Optional<User> findById(long telegramId) {
    try {
      return Optional.of(entityManager.find(User.class, telegramId));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  @Override
  public void save(User user) {
    entityManager.persist(user);
  }

  @Override
  public User getOrCreate(long telegramId) {
    try {
      return (User) entityManager.createQuery("FROM User u WHERE telegramId = ?1")
          .setParameter(1, telegramId).getSingleResult();
    } catch (NoResultException e) {
      User user = context.getBean(User.class, telegramId, "en");
      entityManager.persist(user);
      return user;
    }
  }
}
