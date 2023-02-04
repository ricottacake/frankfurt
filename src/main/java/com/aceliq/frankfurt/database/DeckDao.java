package com.aceliq.frankfurt.database;

import java.util.List;
import java.util.Optional;
import com.aceliq.frankfurt.exceptions.DeckAlreadyExistsException;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.User;

public interface DeckDao {
  public void removeByNameAndOwner(String name, User owner) throws DeckAlreadyExistsException;
  public void createDeckByNameAndOwner(String name, User owner) throws DeckAlreadyExistsException;
  List<Deck> getDecks(User owner);
  Optional<Deck> getDeck(User owner, String name);
}
