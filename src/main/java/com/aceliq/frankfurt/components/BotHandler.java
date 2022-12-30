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
    
    long telegramId = message.getFrom().getId();
    List<SendMessage> forExecute = new ArrayList<>();
    User user;
    
    Optional<User> findUser = userRepository.findById(telegramId);
    
    if(findUser.isEmpty()) {
      user = createUser(telegramId);
    } else {
      user = findUser.get();
    }
    System.out.println(message.getText());
    if(message.getText().equals("/start")) {
      SendMessage sendMessage = new SendMessage();
      sendMessage.setText("Hello!\n"
          + "This bot will help you learn foreign words with the help of flashcards. You can create a large number of decks of cards and add cards to them with a specific phrase and translation into your own language. In addition, the bot allows you to check how well you learned this deck of cards, to select a deck and click \"Check me!\" the number of correct answers.");
      sendMessage.setChatId(message.getChatId());
      forExecute.add(sendMessage);
    }

    UserState state = userState.getOrDefault(telegramId, UserState.DEFAULT);

    switch (state) {
      case MAINMENU:
        forExecute = messageOnMainMenu(message, user, "en", state);
        break;
      case DECKMENU:
      case CREATE_DECK_NAME:
      case DELETE_DECK_NAME:
      case EXPLORE_DECK_NAME:
        forExecute = messageOnDeckMenu(message, user, "en", state);
        break;
      case EXPLORE_DECK_MENU:
      case CREATE_FRONT_CARD_NAME:
      case CREATE_BACK_CARD_NAME:
      case DELETE_CARD_NAME:
      case LEARN:
        forExecute = messageOnExploreDeckMenu(message, user, "en", state);
        break;
      default:
        userState.put(message.getFrom().getId(), UserState.MAINMENU);
        forExecute.add(General.onBackMenuChoosen(message, user, "en"));
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

  private List<SendMessage> messageOnMainMenu(Message message, User user, String language, UserState state) {
    List<SendMessage> forExecute = new ArrayList<SendMessage>();
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());

    switch (state) {
      case MAINMENU:
        if (message.getText().equals(General.getMyDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DECKMENU);
          forExecute.add(General.onDeckMenuChoosen(message, user, language, deckRepository.findByOwner(user)));
        } else {
          userState.put(user.getTelegramId(), UserState.MAINMENU);
          forExecute.add(General.onBackMenuChoosen(message, user, "en"));
        }
        break;
      default:
        break;
    }
    return forExecute;
  }

  private List<SendMessage> messageOnDeckMenu(Message message, User user, String language,
      UserState state) {

    List<SendMessage> forExecute = new ArrayList<SendMessage>();
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());

    switch (state) {
      case CREATE_DECK_NAME:
        createDeck(message.getText(), user);
        sendMessage.setText("SUCCESS");
        forExecute.add(sendMessage);

        SendMessage a = new SendMessage();
        a.setChatId(user.getTelegramId());
        a.setText(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));

        userState.put(user.getTelegramId(), UserState.DECKMENU);
        forExecute.add(
            General.onDeckMenuChoosen(message, user, language, deckRepository.findByOwner(user)));
        break;
      case DELETE_DECK_NAME:
        deleteDeck(message.getText(), user);
        sendMessage.setText("SUCCESS");
        forExecute.add(sendMessage);
        break;
      case EXPLORE_DECK_NAME:
        String deckList = exploreDeck(message.getText(), user);
        sendMessage.setText(deckList);
        sendMessage.setReplyMarkup(General.getExploreDeckKeyboard(language));
        forExecute.add(sendMessage);
        break;
      case DECKMENU:
        if (message.getText().equals(General.getCreateDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.CREATE_DECK_NAME);
          forExecute = General.onCreateDeckChoosen(message, user, language);
        } else if (message.getText().equals(General.getDeleteDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_DECK_NAME);
          forExecute = General.onDeleteDeckChoosen(message, user, language);
        } else if (message.getText().equals(General.getExploreDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_NAME);
          forExecute = General.onViewDeckChoosen(message, user, language);
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

  private List<SendMessage> messageOnExploreDeckMenu(Message message, User user, String language, UserState state) {
    List<SendMessage> forExecute = new ArrayList<SendMessage>();
    Deck deck = userDeckState.get(user.getTelegramId());

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());

    switch (state) {
      case CREATE_FRONT_CARD_NAME:
        createCard(message.getText(), deck);
        sendMessage.setText("ENETER BACK CARD NAME");
        forExecute.add(sendMessage);
        break;
      case CREATE_BACK_CARD_NAME:
        createCard(message.getText(), deck);
        sendMessage.setText("SUCCESS");
        forExecute.add(sendMessage);

        SendMessage a = new SendMessage();
        a.setChatId(user.getTelegramId());
        a.setText(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));

        forExecute.add(a);
        break;
      case DELETE_CARD_NAME:
        deleteCard(message.getText(), deck);
        sendMessage.setText("SUCCESS");
        forExecute.add(sendMessage);
        break;
      case EXPLORE_DECK_MENU:
        if (message.getText().equals(General.getCreateCardCommand(language))) {
          userState.put(user.getTelegramId(), UserState.CREATE_FRONT_CARD_NAME);
          forExecute = General.onCreateCardChoosen(message, user, language);
        } else if (message.getText().equals(General.getDeleteCardCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_DECK_NAME);
          forExecute = General.onDeleteCardChoosen(message, user, language);
        } else if (message.getText().equals(General.getStudyDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.LEARN);
          studyDeck(deck);
        } else {
          userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_MENU);
          SendMessage b = new SendMessage();
          b.setChatId(user.getTelegramId());
          b.setText(exploreDeck(userDeckState.get(user.getTelegramId()).getName(), user));
          forExecute.add(b);
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

  public void createCard(String cardName, Deck deck) {

    long telegramId = deck.getOwner().getTelegramId();

    if (userState.get(telegramId).equals(UserState.CREATE_FRONT_CARD_NAME)) {
      Card card = new Card();
      card.setFront(cardName);
      card.setAddingTime(General.getUnixTimeInSeconds());
      card.setDeck(userDeckState.get(telegramId));
      cardBuffer.put(telegramId, card);

      userState.put(telegramId, UserState.CREATE_BACK_CARD_NAME);
    } else if (userState.get(telegramId).equals(UserState.CREATE_BACK_CARD_NAME)) {
      Card card = cardBuffer.get(telegramId);
      card.setBack(cardName);
      cardRepository.save(card);

      userState.put(telegramId, UserState.EXPLORE_DECK_MENU);
    }
  }

  public void deleteCard(String front, Deck deck) {
    cardRepository.removeByFrontAndDeck(front, deck);
  }

  public void createDeck(String deckName, User owner) {
    Deck deck = context.getBean(Deck.class);
    deck.setName(deckName);
    deck.setOwner(owner);
    deckRepository.save(deck);
  }

  public void deleteDeck(String deckName, User owner) {
    deckRepository.removeByNameAndOwner(deckName, owner);
  }

  public String exploreDeck(String deckName, User user) {

    Optional<Deck> deck = deckRepository.findByOwnerAndName(user, deckName);

    if (deck.isEmpty()) {
      return "DECK NOT EXIST";
    }

    userDeckState.put(user.getTelegramId(), deck.get());
    userState.put(user.getTelegramId(), UserState.EXPLORE_DECK_MENU);

    List<Card> cards = cardRepository.findByDeck(deck.get());

    String text = "";

    for (Card card : cards) {
      text = text + card.getFront() + " : " + card.getBack() + "\n";
    }

    return text.isEmpty() ? "EMPTY" : text;
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
