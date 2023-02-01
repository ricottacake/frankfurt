package com.aceliq.frankfurt.database;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.aceliq.frankfurt.exceptions.DeckAlreadyExistsException;
import com.aceliq.frankfurt.models.User;

public interface DeckDao {
  public void removeByNameAndOwner(String name, User owner) throws DeckAlreadyExistsException;
  public void createDeckByNameAndOwner(String name, User owner) throws DeckAlreadyExistsException;

}
