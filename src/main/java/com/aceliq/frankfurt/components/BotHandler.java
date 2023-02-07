package com.aceliq.frankfurt.components;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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

  private void handleIncomingMessage(Message message) {
    List<SendMessage> forExecute = new ArrayList<>();
    long telegramId = message.getFrom().getId();
    User user = userDaoImpl.findById(telegramId)
        .orElseGet(() -> context.getBean(User.class, telegramId, "en"));

    if (message.getText().equals("/start")) {
      forExecute.add(General.onStart(message, user));
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
        userState.put(user.getTelegramId(), UserState.MAINMENU);
        forExecute.add(General.onBackMenuChoosen(message, user));
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
          userState.put(user.getTelegramId(), UserState.DECKMENU);
          forExecute.add(General.onDeckMenuChoosen(message, user, user.getLanguage().toString(),
              deckDaoImpl.getDecks(user)));
        } else {
          userState.put(user.getTelegramId(), UserState.MAINMENU);
          forExecute.add(General.onBackMenuChoosen(message, user));
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
        userState.put(user.getTelegramId(), UserState.DECKMENU);
        forExecute.add(General.getSuccessMessage(user));
        forExecute
            .add(General.onDeckMenuChoosen(message, user, language, deckDaoImpl.getDecks(user)));
        break;
      case DELETE_DECK_NAME:
        try {
          deckDaoImpl.removeByNameAndOwner(message.getText(), user);
        } catch (DeckAlreadyExistsException e) {
          forExecute.add(General.getDeckNotExistMessage(user));
          break;
        }
        userState.put(user.getTelegramId(), UserState.DECKMENU);
        forExecute.add(General.getSuccessMessage(user));
        forExecute
            .add(General.onDeckMenuChoosen(message, user, language, deckDaoImpl.getDecks(user)));
        break;
      case EXPLORE_DECK_NAME:
        forExecute.add(exploreDeck(message.getText(), user));
        break;
      case DECKMENU:
        if (message.getText().equals(General.getCreateDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.CREATE_DECK_NAME);
          forExecute = General.onCreateDeckChoosen(message, user);
        } else if (message.getText().equals(General.getDeleteDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_DECK_NAME);
          forExecute = General.onDeleteDeckChoosen(message, user);
        } else if (message.getText().equals(General.getExploreDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_NAME);
          forExecute = General.onViewDeckChoosen(message, user);
        } else if (message.getText().equals(General.getMenuCommand(language))) {
          userState.put(user.getTelegramId(), UserState.MAINMENU);
          forExecute.add(General.onBackMenuChoosen(message, user));
        } else {
          userState.put(user.getTelegramId(), UserState.DECKMENU);
          forExecute
              .add(General.onDeckMenuChoosen(message, user, language, deckDaoImpl.getDecks(user)));
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
        forExecute.add(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));
        break;
      case DELETE_CARD_NAME:
        card = deckBuffer.get(user.getTelegramId()).get(Integer.parseInt(message.getText()) - 1);
        cardDaoImpl.removeByFrontAndDeck(card.getFront(), deck);
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));
        break;
      case EXPLORE_DECK_MENU:
        if (message.getText().equals(General.getCreateCardCommand(language))) {
          card = context.getBean(Card.class);
          card.setAddingTime(General.getUnixTimeInSeconds());
          card.setDeck(userDeckState.get(telegramId));
          cardBuffer.put(telegramId, card);
          userState.put(user.getTelegramId(), UserState.CREATE_FRONT_CARD_NAME);
          forExecute = General.onCreateCardChoosen(message, user);
        } else if (message.getText().equals(General.getDeleteCardCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_CARD_NAME);
          forExecute = General.onDeleteCardChoosen(message, user);
        } else if (message.getText().equals(General.getStudyDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.LEARN);
          studyDeck.start(deck);
        } else if (message.getText().equals(General.getMenuCommand(language))) {
          userState.put(user.getTelegramId(), UserState.MAINMENU);
          forExecute.add(General.onBackMenuChoosen(message, user));
        } else {
          userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_MENU);
          forExecute.add(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));
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

  public SendMessage exploreDeck(String deckName, User user) {
    Optional<Deck> deck = deckDaoImpl.getDeck(user, deckName);
    userDeckState.put(user.getTelegramId(), deck.get());
    userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_MENU);
    List<Card> cards = cardDaoImpl.getCards(deck.get());
    deckBuffer.put(user.getTelegramId(), cards);
    return General.convertDeckListToMessage(cards, user);
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
