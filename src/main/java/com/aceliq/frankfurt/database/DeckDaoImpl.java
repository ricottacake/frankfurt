package com.aceliq.frankfurt.database;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.aceliq.frankfurt.exceptions.DeckAlreadyExistsException;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.User;

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW , rollbackFor = {Throwable.class} )
public class DeckDaoImpl implements DeckDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public void removeByNameAndOwner(String name, User owner) throws DeckAlreadyExistsException {
    Query q = entityManager.createNativeQuery("SELECT id FROM decks WHERE name = ?");
    q.setParameter(1, name);
    
    int i = 0;
    try {
      i = (Integer) q.getSingleResult();
    } catch (NoResultException e) {
      throw new DeckAlreadyExistsException("ok");
    }
    
    q = entityManager.createNativeQuery("DELETE FROM cards WHERE deck_id = ?");
    q.setParameter(1, i);
    q.executeUpdate();

    q = entityManager.createNativeQuery("DELETE FROM decks WHERE name = ? AND user_id = ?");
    q.setParameter(1, name);
    q.setParameter(2, owner.getTelegramId());
    q.executeUpdate();
  }

  @Override
  public void createDeckByNameAndOwner(String name, User owner) throws DeckAlreadyExistsException {
    Query q = entityManager.createNativeQuery("INSERT INTO decks (name, user_id) VALUES (?, ?)");
    q.setParameter(1, name);
    q.setParameter(2, owner.getTelegramId());
    try {
      q.executeUpdate();
    } catch(PersistenceException e) {
      throw new DeckAlreadyExistsException("ok");
    }
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public List<Deck> getDecks(User owner) {
    Query query = entityManager.createQuery("SELECT a FROM Deck a WHERE owner = ?1", Deck.class);
    query.setParameter(1, owner);
    return query.getResultList();
  }

  @Override
  public Optional<Deck> getDeck(User owner, String name) {
    Query query = entityManager.createQuery("SELECT a FROM Deck a WHERE owner = ?1 AND name = ?2", Deck.class);
    query.setParameter(1, owner);
    query.setParameter(2, name);
    Deck y = (Deck) query.getSingleResult();
    return Optional.ofNullable(y);
  }
}
