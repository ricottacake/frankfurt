package com.aceliq.frankfurt.database;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.aceliq.frankfurt.exceptions.DeckAlreadyExistsException;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW , rollbackFor = {Throwable.class} )
public class CardDaoImpl implements CardDao {
  
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @SuppressWarnings(value = "unchecked")
  public List<Card> getCards(Deck deck) {
    Query query = entityManager.createQuery("SELECT a FROM Card a WHERE deck = ?1", Card.class);
    query.setParameter(1, deck);
    return query.getResultList();
  }

  @Override
  public void removeByFrontAndDeck(String front, Deck deck) {
    Query query = entityManager.createQuery("DELETE FROM Card WHERE front = ?1 AND deck = ?2");
    query.setParameter(1, front);
    query.setParameter(2, deck);
    query.executeUpdate(); 
  }

  @Override
  public void createCard(Card card) throws DeckAlreadyExistsException {
    entityManager.persist(card);
  }
}
