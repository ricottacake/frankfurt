package com.aceliq.frankfurt.main.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.aceliq.frankfurt.database.DeckDaoImpl;
import com.aceliq.frankfurt.database.DeckRepository;
import com.aceliq.frankfurt.database.UserRepository;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.User;


@RunWith(SpringRunner.class)
@DataJpaTest
public class DeckRepositoryIntegrationTest {
  
  @Autowired
  DeckRepository deckRepository;
  
  @Autowired
  UserRepository userRepository;
  
  @Autowired
  DeckDaoImpl deckDaoImpl;
  
  User testUser;
  
  @Ignore
  @Before
  public void setUp() {
    User user = new User();
    user.setJoinDate(111);
    user.setTelegramId(555);
    user.setLanguage("en");
    testUser = user;
  }
  
  @Test
  public void createDeckTest() {
    User user = new User();
    user.setTelegramId(374732026);
    userRepository.save(user);
    //deckDaoImpl.createDeckByNameAndOwner("deckName", user);
    //deckDaoImpl.createDeckByNameAndOwner("deckName", user);
  }
  
  @Ignore
  @Test
  public void createDeckAndFindDeck() {
    
    userRepository.save(testUser);
    
    Deck deck1 = new Deck();
    deck1.setName("deckName_1");
    deck1.setOwner(testUser);
    
    Deck deck2 = new Deck();
    deck2.setName("deckName_2");
    deck2.setOwner(testUser);

    Deck deck3 = new Deck();
    deck3.setName("deckName_3");
    deck3.setOwner(testUser);
    
    deckRepository.save(deck1);
    deckRepository.save(deck2);
    deckRepository.save(deck3);
    
    Optional<Deck> s1 = deckRepository.findByOwnerAndName(testUser, "deckName_1");
    Optional<Deck> s2 = deckRepository.findByOwnerAndName(testUser, "deckName_2");
    Optional<Deck> s3 = deckRepository.findByOwnerAndName(testUser, "deckName_3");
    
    assertEquals(deck1.getName(), s1.get().getName());
    assertEquals(deck2.getName(), s2.get().getName());
    assertEquals(deck3.getName(), s3.get().getName());
  }
}
