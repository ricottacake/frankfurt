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
import com.aceliq.frankfurt.database.CardRepository;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.TimeIsOver;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.UserState;

@Component
public class StudyDeck {

  private HashMap<Long, ArrayDeque<Card>> userTable = new HashMap<>();
  //private HashMap<Long, Integer> score = new HashMap<>();
  private HashMap<Long, ScheduledFuture<?>> userFuture = new HashMap<>();

  private ApplicationContext context;
  private CardRepository cardRepository;
  private ThreadPoolTaskScheduler taskScheduler;
  private BotHandler botHandler;

  public StudyDeck(ApplicationContext context, CardRepository wordRepository,
      ThreadPoolTaskScheduler taskScheduler, @Lazy BotHandler botHandler) {
    this.context = context;
    this.cardRepository = wordRepository;
    this.taskScheduler = taskScheduler;
    this.botHandler = botHandler;
  }

  public void nextCard(User user) {
    if (userTable.get(user.getTelegramId()).isEmpty()) {
      finishStudy(user);
      return;
    }

    SendMessage message = new SendMessage();
    message.setText(userTable.get(user.getTelegramId()).getFirst().getFront());
    message.setChatId(user.getTelegramId());

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
    message.setText("FINISHED");
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
      answer.setText("TRUE");
    } else {
      answer.setText("FALSE");
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

    List<Card> cards = cardRepository.findByDeck(deck);
    Collections.shuffle(cards);
    userTable.put(deck.getOwner().getTelegramId(), new ArrayDeque<Card>(cards));
    nextCard(deck.getOwner());
  }
}
