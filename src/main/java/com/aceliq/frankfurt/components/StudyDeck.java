package com.aceliq.frankfurt.components;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.TimeIsOver;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.UserState;
import com.aceliq.frankfurt.util.General;

@Component
public class StudyDeck {

  private HashMap<Long, List<Card>> userTableInit = new HashMap<>();
  private HashMap<Long, ScheduledFuture<?>> userFuture = new HashMap<>();
  private HashMap<Long, Integer> userPointers = new HashMap<>();

  private ApplicationContext context;
  private ThreadPoolTaskScheduler taskScheduler;
  private BotHandler botHandler;

  public StudyDeck(ApplicationContext context, ThreadPoolTaskScheduler taskScheduler,
      @Lazy BotHandler botHandler) {
    this.context = context;
    this.taskScheduler = taskScheduler;
    this.botHandler = botHandler;
  }

  public void nextCard(User user) {
    if (userTableInit.get(user.getTelegramId()).size() == userPointers.get(user.getTelegramId())) {
      finishStudy(user);
      return;
    }

    Card w = userTableInit.get(user.getTelegramId()).get(userPointers.get(user.getTelegramId()));
    List<Card> table = userTableInit.get(user.getTelegramId());
    int pointer = userPointers.get(user.getTelegramId());
    Collections.shuffle(table);

    List<Card> d = Stream.concat(Stream.of(w), table.stream()).distinct().limit(4)
        .collect(Collectors.toList());

    Collections.shuffle(d);

    SendMessage message = General.getQuestionMessage(user, table.get(pointer).getFront(), d);
    userPointers.put(user.getTelegramId(), pointer + 1);

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

    try {
      botHandler.execute(message);
      botHandler.setUserState(user, UserState.EXPLORE_DECK_MENU);
      botHandler.execute(
          botHandler.goToExploreDeckMenu(botHandler.getUserDeckState(user.getTelegramId()), user));
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  public int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  public void checkWord(User user, Message message) {
    String expectedWord = userTableInit.get(user.getTelegramId())
        .get(userPointers.get(user.getTelegramId()) - 1).getBack();

    try {
      if (message.getText().equals(expectedWord)) {
        botHandler.execute(General.getRightMessage(user));
      } else {
        botHandler.execute(General.getNoRightMessage(user));
      }
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
    userFuture.get(message.getChatId()).cancel(false);
    nextCard(user);
  }

  public void start(Deck deck) {
    List<Card> cards = deck.getCards();
    Collections.shuffle(cards);
    userPointers.put(deck.getOwner().getTelegramId(), 0);
    userTableInit.put(deck.getOwner().getTelegramId(), cards);
    nextCard(deck.getOwner());
  }
}
