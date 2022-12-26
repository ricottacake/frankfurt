package com.aceliq.frankfurt.main.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import com.aceliq.frankfurt.components.BotHandler;
import com.aceliq.frankfurt.database.CardRepository;
import com.aceliq.frankfurt.database.DeckRepository;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CardRepositoryIntegrationTest {
  
  @Autowired
  CardRepository cardRepository;
  
  @Autowired
  DeckRepository deckRepository;
  
  @After
  public void clean_card() {
    cardRepository.deleteAll();
  }
  
  @Test
  public void deleteCardFromDeck() {
    
  }
  
  @Test
  public void whenFindByDeck_thenReturnListOfCards() {
    
    Deck deck = new Deck();
    deck.setName("testDeck");
    deck.setId(22);
    
    Card card1 = new Card();
    card1.setFront("test");
    card1.setBack("тест");
    card1.setDeck(deck);
    
    Card card2 = new Card();
    card2.setFront("check");
    card2.setBack("проверка");
    card2.setDeck(deck);
    
    Card card3 = new Card();
    card3.setFront("point");
    card3.setBack("точка");
    card3.setDeck(deck);
    
    cardRepository.save(card1);
    cardRepository.save(card2);
    cardRepository.save(card3);
    
    List<Card> cards = cardRepository.findByDeck(deck);
    assertThat(cards.get(0).getFront()).isEqualTo(card1.getFront());
    assertThat(cards.get(1).getFront()).isEqualTo(card2.getFront());
    assertThat(cards.get(2).getFront()).isEqualTo(card3.getFront());
    
    assertThat(cards.get(0).getBack()).isEqualTo(card1.getBack());
    assertThat(cards.get(1).getBack()).isEqualTo(card2.getBack());
    assertThat(cards.get(2).getBack()).isEqualTo(card3.getBack());
    
  }
}
