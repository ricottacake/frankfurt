package com.aceliq.frankfurt.database;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;

@Repository
public interface CardRepository extends CrudRepository<Card, Integer> {
  List<Card> findByDeck(Deck deck);
  void removeByFrontAndDeck(String front, Deck deck);
}
