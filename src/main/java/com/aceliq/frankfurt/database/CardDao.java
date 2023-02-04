package com.aceliq.frankfurt.database;

import java.util.List;
import com.aceliq.frankfurt.exceptions.DeckAlreadyExistsException;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;

public interface CardDao {
  List<Card> getCards(Deck deck);
  void removeByFrontAndDeck(String front, Deck deck);
  void createCard(Card card) throws DeckAlreadyExistsException;
}
