package com.aceliq.frankfurt.components;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;

@Configuration
public class AppConfig {
  
  @Bean
  @Scope(value = "prototype")
  public Card cardBean() {
    return new Card();
  }
  
  @Bean
  @Scope(value = "prototype")
  public User userBean() {
    return new User();
  }
  
  @Bean
  @Scope(value = "prototype")
  public Deck deckBean() {
    return new Deck();
  }
}
