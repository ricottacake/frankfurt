package com.aceliq.frankfurt.components;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.Word;

@Configuration
public class AppConfig {
  
  @Bean
  @Scope(value = "prototype")
  public Word wordBean() {
    return new Word();
  }
  
  @Bean
  @Scope(value = "prototype")
  public User userBean() {
    return new User();
  }
}
