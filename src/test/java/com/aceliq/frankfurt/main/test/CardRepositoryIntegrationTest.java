package com.aceliq.frankfurt.main.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CardRepositoryIntegrationTest {

  
  @Test
  public void deleteCardFromDeck() {
    
  }
  
  @Test
  public void whenFindByDeck_thenReturnListOfCards() {
   

    
  }
}
