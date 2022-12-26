package com.aceliq.frankfurt.components;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.TimeIsOver;

@Configuration
public class AppConfig {

  @Scope(value = "prototype")
  @Bean
  public TimeIsOver runnableTaskBean(User user) {
    return new TimeIsOver(user);
  }
  
  @Bean
  public Deck deck() {
    return new Deck();
  }
}
