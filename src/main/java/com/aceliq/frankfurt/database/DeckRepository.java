package com.aceliq.frankfurt.database;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.User;

@Repository
@Transactional
public interface DeckRepository extends CrudRepository<Deck, Integer>  {
  List<Deck> findByOwner(User owner);
  Deck findByOwnerAndName(User owner, String name);
  List<Deck> removeByNameAndOwner(String name, User owner);
}
