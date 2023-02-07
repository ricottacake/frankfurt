package com.aceliq.frankfurt.components;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.aceliq.frankfurt.database.CardDaoImpl;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.TimeIsOver;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.UserState;
import com.aceliq.frankfurt.util.General;

@Component
public class StudyDeck {

  private HashMap<Long, List<Card>> userTableInit = new HashMap<>();
  private HashMap<Long, ArrayDeque<Card>> userTable = new HashMap<>();
  private HashMap<Long, ScheduledFuture<?>> userFuture = new HashMap<>();

  private ApplicationContext context;
  private CardDaoImpl cardDaoImpl;
  private ThreadPoolTaskScheduler taskScheduler;
  private BotHandler botHandler;

  public StudyDeck(ApplicationContext context, ThreadPoolTaskScheduler taskScheduler,
      @Lazy BotHandler botHandler, CardDaoImpl cardDaoImpl) {
    this.context = context;
    this.taskScheduler = taskScheduler;
    this.botHandler = botHandler;
    this.cardDaoImpl = cardDaoImpl;
  }

  public void nextCard(User user) {
    if (userTable.get(user.getTelegramId()).isEmpty()) {
      finishStudy(user);
      return;
    }

    String[] options = new String[4];
    options[0] = General.getRandomBackNameFromList(userTableInit.get(user.getTelegramId()));
    options[1] = General.getRandomBackNameFromList(userTableInit.get(user.getTelegramId()));
    options[2] = General.getRandomBackNameFromList(userTableInit.get(user.getTelegramId()));
    options[3] = General.getRandomBackNameFromList(userTableInit.get(user.getTelegramId()));

    SendMessage message = General.getQuestionMessage(user,
        userTable.get(user.getTelegramId()).getFirst().getFront(), options);
    
    try {
      botHandler.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }

    userFuture.put(user.getTelegramId(), taskScheduler
        .schedule(context.getBean(TimeIsOver.class, user), Instant.now().plusSeconds(5)));
  }

  public void finishStudy(User user) {
    SendMessage message = new SendMessage();
    message.setText("That's all");
    message.setChatId(user.getTelegramId());

    botHandler.setUserState(user, UserState.EXPLORE_DECK_MENU);

    try {
      botHandler.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  public int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  public void removeFirst(User user) {
    userTable.get(user.getTelegramId()).removeFirst();
  }

  public void checkWord(User user, Message message) {
    String expectedWord = userTable.get(message.getChatId()).getFirst().getBack();
    SendMessage answer = new SendMessage();
    answer.setChatId(message.getChatId());
    if (message.getText().equals(expectedWord)) {
      answer.setText("Right");
    } else {
      answer.setText("Not right");
    }
    try {
      botHandler.execute(answer);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
    userFuture.get(message.getChatId()).cancel(false);
    removeFirst(user);
    nextCard(user);
  }

  public void start(Deck deck) {
    List<Card> cards = cardDaoImpl.getCards(deck);
    Collections.shuffle(cards);
    userTable.put(deck.getOwner().getTelegramId(), new ArrayDeque<Card>(cards));
    userTableInit.put(deck.getOwner().getTelegramId(), cards);
    nextCard(deck.getOwner());
  }
}
