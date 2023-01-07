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

import com.aceliq.frankfurt.database.UserRepository;
import com.aceliq.frankfurt.database.CardRepository;
import com.aceliq.frankfurt.database.DeckRepository;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.UserState;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.util.General;

@Component
public class BotHandler extends TelegramLongPollingBot {

  private HashMap<Long, Deck> userDeckState = new HashMap<Long, Deck>();
  private HashMap<Long, UserState> userState = new HashMap<Long, UserState>();
  private HashMap<Long, Card> cardBuffer = new HashMap<Long, Card>();

  @Value("${TELEGRAM_BOT_KEY}")
  private String botToken;

  @Value("${TELEGRAM_BOT_USERNAME}")
  private String botUsername;

  private ApplicationContext context;
  private CardRepository cardRepository;
  private UserRepository userRepository;
  private DeckRepository deckRepository;
  private StudyDeck studyDeck;

  public BotHandler(ApplicationContext context, CardRepository wordRepository,
      UserRepository userRepository, DeckRepository deckRepository, StudyDeck studyDeck) {
    this.context = context;
    this.cardRepository = wordRepository;
    this.userRepository = userRepository;
    this.deckRepository = deckRepository;
    this.studyDeck = studyDeck;
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
    User user = userRepository.findById(telegramId).orElseGet(() -> createUser(telegramId));

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
              deckRepository.findByOwner(user)));
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
        Deck newDeck = createDeck(message.getText(), user);
        userDeckState.put(user.getTelegramId(), newDeck);
        userState.put(user.getTelegramId(), UserState.DECKMENU);
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(
            General.onDeckMenuChoosen(message, user, language, deckRepository.findByOwner(user)));
        break;
      case DELETE_DECK_NAME:
        deleteDeck(message.getText(), user);
        userState.put(user.getTelegramId(), UserState.DECKMENU);
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(
            General.onDeckMenuChoosen(message, user, language, deckRepository.findByOwner(user)));
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
          forExecute.add(
              General.onDeckMenuChoosen(message, user, language, deckRepository.findByOwner(user)));
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

    switch (state) {
      case CREATE_FRONT_CARD_NAME:
        createCard(message.getText(), deck);
        forExecute.add(General.getEnterBackCardNameMessage(user));
        break;
      case CREATE_BACK_CARD_NAME:
        createCard(message.getText(), deck);
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));
        break;
      case DELETE_CARD_NAME:
        deleteCard(message.getText(), deck);
        forExecute.add(General.getSuccessMessage(user));
        forExecute.add(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));
        break;
      case EXPLORE_DECK_MENU:
        if (message.getText().equals(General.getCreateCardCommand(language))) {
          userState.put(user.getTelegramId(), UserState.CREATE_FRONT_CARD_NAME);
          forExecute = General.onCreateCardChoosen(message, user);
        } else if (message.getText().equals(General.getDeleteCardCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_CARD_NAME);
          forExecute = General.onDeleteCardChoosen(message, user);
        } else if (message.getText().equals(General.getStudyDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.LEARN);
          studyDeck(deck);
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

  public User createUser(long telegramId) {
    User user = context.getBean(User.class);
    user.setTelegramId(telegramId);
    user.setJoinDate(Instant.now().getEpochSecond());
    userRepository.save(user);
    return user;
  }

  public void createCard(String string, Deck deck) {
    long telegramId = deck.getOwner().getTelegramId();
    if (userState.get(telegramId).equals(UserState.CREATE_FRONT_CARD_NAME)) {
      Card card = context.getBean(Card.class);
      card.setFront(string);
      card.setAddingTime(General.getUnixTimeInSeconds());
      card.setDeck(userDeckState.get(telegramId));
      cardBuffer.put(telegramId, card);
      userState.put(telegramId, UserState.CREATE_BACK_CARD_NAME);
    } else if (userState.get(telegramId).equals(UserState.CREATE_BACK_CARD_NAME)) {
      Card card = cardBuffer.get(telegramId);
      card.setBack(string);
      cardRepository.save(card);
      userState.put(telegramId, UserState.EXPLORE_DECK_MENU);
    }
  }

  public void deleteCard(String front, Deck deck) {
    cardRepository.removeByFrontAndDeck(front, deck);
  }

  public Deck createDeck(String deckName, User owner) {
    Deck deck = context.getBean(Deck.class);
    deck.setName(deckName);
    deck.setOwner(owner);
    deckRepository.save(deck);
    return deck;
  }

  public void deleteDeck(String deckName, User owner) {
    deckRepository.removeByNameAndOwner(deckName, owner);
  }

  public SendMessage exploreDeck(String deckName, User user) {
    Optional<Deck> deck = deckRepository.findByOwnerAndName(user, deckName);
    userDeckState.put(user.getTelegramId(), deck.get());
    userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_MENU);
    List<Card> cards = cardRepository.findByDeck(deck.get());
    return General.convertDeckListToMessage(cards, user);
  }

  public void studyDeck(Deck deck) {
    studyDeck.start(deck);
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
