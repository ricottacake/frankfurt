package com.aceliq.frankfurt.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.aceliq.frankfurt.exceptions.DeckAlreadyExistsException;
import com.aceliq.frankfurt.database.CardDaoImpl;
import com.aceliq.frankfurt.database.DeckDaoImpl;
import com.aceliq.frankfurt.database.UserDaoImpl;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.UserState;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.util.General;

@Component
public class BotHandler extends TelegramLongPollingBot {

  private HashMap<Long, Deck> userDeckState = new HashMap<>();
  private HashMap<Long, UserState> userState = new HashMap<>();
  private HashMap<Long, Card> cardBuffer = new HashMap<>();
  private HashMap<Long, List<Card>> deckBuffer = new HashMap<>();
  private HashMap<Long, Stack<UserState>> userNav = new HashMap<>();

  @Value("${TELEGRAM_BOT_KEY}")
  private String botToken;

  @Value("${TELEGRAM_BOT_USERNAME}")
  private String botUsername;

  private ApplicationContext context;
  private UserDaoImpl userDaoImpl;
  private StudyDeck studyDeck;
  private DeckDaoImpl deckDaoImpl;
  private CardDaoImpl cardDaoImpl;

  public BotHandler(ApplicationContext context, CardDaoImpl cardDaoImpl, UserDaoImpl userDaoImpl,
      StudyDeck studyDeck, DeckDaoImpl deckDaoImpl) {
    this.context = context;
    this.cardDaoImpl = cardDaoImpl;
    this.userDaoImpl = userDaoImpl;
    this.studyDeck = studyDeck;
    this.deckDaoImpl = deckDaoImpl;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      Message message = update.getMessage();
      handleIncomingMessage(message);
    }
  }

  public void setUserState(User user, UserState userState) {
    this.userState.put(user.getTelegramId(), userState);
  }
  
  public Deck getUserDeckState(long telegramId) {
    return userDeckState.get(telegramId);
  }

  private void handleIncomingMessage(Message message) {
    List<SendMessage> forExecute = new ArrayList<>();
    long telegramId = message.getFrom().getId();
    User user = userDaoImpl.findById(telegramId)
        .orElseGet(() -> context.getBean(User.class, telegramId, "en"));
    userNav.putIfAbsent(user.getTelegramId(), new Stack<UserState>());

    if (message.getText().equals("/start")) {
      forExecute.add(General.getStartMessage(user));
      userState.put(user.getTelegramId(), UserState.DEFAULT);
    }

    UserState state = userState.getOrDefault(telegramId, UserState.DEFAULT);

    switch (state) {
      case MAINMENU:
        forExecute = messageOnMainMenu(message, user, state);
        break;
      case DECKMENU:
      case CREATE_DECK_NAME:
      case DELETE_DECK_NAME:
      case EXPLORE_DECK_NAME:
        forExecute = messageOnDeckMenu(message, user, state);
        break;
      case EXPLORE_DECK_MENU:
      case CREATE_FRONT_CARD_NAME:
      case CREATE_BACK_CARD_NAME:
      case DELETE_CARD_NAME:
      case LEARN:
        forExecute = messageOnExploreDeckMenu(message, user, state);
        break;
      case DEFAULT:
        forExecute.add(goToMainMenu(user));
        break;
    }
    for (SendMessage i : forExecute) {
      try {
        execute(i);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    }
  }

  private List<SendMessage> messageOnMainMenu(Message message, User user, UserState state) {
    List<SendMessage> forExecute = new ArrayList<SendMessage>();
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());

    switch (state) {
      case MAINMENU:
        if (message.getText().equals(General.getMyDecksCommand(user.getLanguage().toString()))) {
          forExecute.add(goToDeckMenu(user));
        } else if(message.getText().equals("Back")) {
          forExecute.add(goBack(user));
        } else {
          forExecute.add(goToMainMenu(user));
        }
        break;
      default:
        break;
    }
    return forExecute;
  }

  private List<SendMessage> messageOnDeckMenu(Message message, User user, UserState state) {

    List<SendMessage> forExecute = new ArrayList<SendMessage>();
    String language = user.getLanguage().toString();

    switch (state) {
      case CREATE_DECK_NAME:
        try {
          deckDaoImpl.createDeckByNameAndOwner(message.getText(), user);
        } catch (DeckAlreadyExistsException e) {
          forExecute.add(General.getDeckExistMessage(user));
          break;
        }

        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(goToDeckMenu(user));
        break;
      case DELETE_DECK_NAME:
        try {
          deckDaoImpl.removeByNameAndOwner(message.getText(), user);
        } catch (DeckAlreadyExistsException e) {
          forExecute.add(General.getDeckNotExistMessage(user));
          break;
        }
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(goToDeckMenu(user));
        break;
      case EXPLORE_DECK_NAME:
        forExecute
            .add(goToExploreDeckMenu(deckDaoImpl.getDeck(user, message.getText()).get(), user));
        break;
      case DECKMENU:
        if (message.getText().equals(General.getCreateDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.CREATE_DECK_NAME);
          forExecute = General.onCreateDeckChoosen(user);
        } else if (message.getText().equals(General.getDeleteDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_DECK_NAME);
          forExecute = General.onDeleteDeckChoosen(user);
        } else if (message.getText().equals(General.getExploreDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_NAME);
          forExecute = General.onViewDeckChoosen(user, deckDaoImpl.getDecks(user));
        } else if (message.getText().equals(General.getMenuCommand(language))) {
          forExecute.add(goToMainMenu(user));
        } else if(message.getText().equals("Back")) {
          forExecute.add(goBack(user));
        } else {
          forExecute.add(goToDeckMenu(user));
        }
        break;
      default:
        break;
    }
    return forExecute;
  }

  private List<SendMessage> messageOnExploreDeckMenu(Message message, User user, UserState state) {
    List<SendMessage> forExecute = new ArrayList<SendMessage>();
    Deck deck = userDeckState.get(user.getTelegramId());
    String language = user.getLanguage().toString();
    long telegramId = deck.getOwner().getTelegramId();
    Card card;
    switch (state) {
      case CREATE_FRONT_CARD_NAME:
        card = cardBuffer.get(telegramId);
        card.setFront(message.getText());
        userState.put(telegramId, UserState.CREATE_BACK_CARD_NAME);
        forExecute.add(General.getEnterBackCardNameMessage(user));
        break;
      case CREATE_BACK_CARD_NAME:
        card = cardBuffer.get(telegramId);
        card.setBack(message.getText());
        userState.put(telegramId, UserState.EXPLORE_DECK_MENU);
        try {
          cardDaoImpl.createCard(card);
        } catch (DeckAlreadyExistsException e) {
        }
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(goToExploreDeckMenu(deck, user));
        break;
      case DELETE_CARD_NAME:
        card = deckBuffer.get(user.getTelegramId()).get(Integer.parseInt(message.getText()) - 1);
        cardDaoImpl.removeByFrontAndDeck(card.getFront(), deck);
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(goToExploreDeckMenu(deck, user));
        break;
      case EXPLORE_DECK_MENU:
        if (message.getText().equals(General.getCreateCardCommand(language))) {
          card = context.getBean(Card.class);
          card.setAddingTime(General.getUnixTimeInSeconds());
          card.setDeck(deck);
          cardBuffer.put(telegramId, card);
          userState.put(telegramId, UserState.CREATE_FRONT_CARD_NAME);
          forExecute = General.onCreateCardChoosen(user);
        } else if (message.getText().equals(General.getDeleteCardCommand(language))) {
          userState.put(telegramId, UserState.DELETE_CARD_NAME);
          forExecute = General.onDeleteCardChoosen(user);
        } else if (message.getText().equals(General.getStudyDeckCommand(language))) {
          userState.put(telegramId, UserState.LEARN);
          studyDeck.start(deck);
        } else if (message.getText().equals(General.getMenuCommand(language))) {
          forExecute.add(goToMainMenu(user));
        } else if(message.getText().equals("Back")) {
          forExecute.add(goBack(user));
        } else {
          forExecute.add(goToExploreDeckMenu(deck, user));
        }
        break;
      case LEARN:
        studyDeck.checkWord(user, message);
        break;
      default:
        break;
    }
    return forExecute;
  }

  public SendMessage goToExploreDeckMenu(Deck deck, User user) {
    userNav.get(user.getTelegramId()).push(UserState.EXPLORE_DECK_MENU);
    userDeckState.put(user.getTelegramId(), deck);
    userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_MENU);
    List<Card> cards = deck.getCards();
    deckBuffer.put(user.getTelegramId(), cards);
    return General.getDeckListMessage(user, cards);
  }

  public SendMessage goToDeckMenu(User user) {
    userNav.get(user.getTelegramId()).push(UserState.DECKMENU);
    userState.put(user.getTelegramId(), UserState.DECKMENU);
    return General.getMyDeckMessage(user, deckDaoImpl.getDecks(user));
  }

  public SendMessage goToMainMenu(User user) {
    userNav.get(user.getTelegramId()).push(UserState.MAINMENU);
    userState.put(user.getTelegramId(), UserState.MAINMENU);
    return General.getOnMenuMessage(user);
  }
  
  public SendMessage goBack(User user) {
    UserState a = userNav.get(user.getTelegramId()).pop();
    UserState to = userNav.get(user.getTelegramId()).pop();
    
    switch(to) {
      case MAINMENU:
        return goToMainMenu(user);
      case DECKMENU:
        return goToDeckMenu(user);
      default:
        return null;
    }
  }

  @Override
  public String getBotUsername() {
    return botUsername;
  }

  @Override
  public String getBotToken() {
    return botToken;
  }
}
