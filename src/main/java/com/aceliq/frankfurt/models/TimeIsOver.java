package com.aceliq.frankfurt.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.aceliq.frankfurt.components.StudyDeck;

public class TimeIsOver implements Runnable {
  
  @Autowired
  private ApplicationContext context;
  private User user;
  
  public TimeIsOver(User user) {
    this.user = user;
  }

  @Override
  public void run() {
    //context.getBean(StudyDeck.class).removeFirst(user);
    context.getBean(StudyDeck.class).nextCard(user);
  }
}
