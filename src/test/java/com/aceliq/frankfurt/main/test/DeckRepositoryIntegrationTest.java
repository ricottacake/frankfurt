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
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.User;


@RunWith(SpringRunner.class)
@DataJpaTest
public class DeckRepositoryIntegrationTest {
  
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

  }
  
  @Ignore
  @Test
  public void createDeckAndFindDeck() {
    
  }
}
