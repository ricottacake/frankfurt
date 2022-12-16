package com.aceliq.frankfurt.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

  public BotHandler(ApplicationContext context, CardRepository wordRepository,
      UserRepository userRepository, DeckRepository deckRepository) {
    this.context = context;
    this.cardRepository = wordRepository;
    this.userRepository = userRepository;
    this.deckRepository = deckRepository;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      Message message = update.getMessage();
      try {
        handleIncomingMessage(message);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    }
  }

  private void handleIncomingMessage(Message message) throws TelegramApiException {

    long telegramId = message.getFrom().getId();
    User user = userRepository.findById(telegramId).get();

    List<SendMessage> forExecute = null;

    if (!message.isUserMessage() && message.hasText()) {
      if (General.isCommandForOther(message.getText())) {
        return;
      }
    }

    UserState state = userState.getOrDefault(telegramId, UserState.DEFAULT);

    switch (state) {
      case MAINMENU:
        forExecute = messageOnMainMenu(message, user, "en", state);
        break;
      case DECKMENU:
      case CREATE_DECK_NAME:
      case DELETE_DECK_NAME:
      case VIEW_DECK_NAME:
        forExecute = messageOnDeckMenu(message, user, "en", state);
        break;
      case CARDMENU:
      case CREATE_FRONT_CARD_NAME:
      case CREATE_BACK_CARD_NAME:
        forExecute = messageOnViewDeckMenu(message, user, "en", state);
        break;
      case DEFAULT:
        userState.put(message.getFrom().getId(), UserState.MAINMENU);
        forExecute = General.onBackMenuChoosen(message, user, "en");
        break;
      default:
        forExecute = null;
        break;
    }

    for (SendMessage i : forExecute) {
      execute(i);
    }
  }

  private List<SendMessage> messageOnMainMenu(Message message, User user, String language,
      UserState state) {
    SendMessage sendMessageRequest = null;
    if (message.hasText()) {
      if (message.getText().equals(General.getMyDeckCommand(language))) {
        userState.put(user.getTelegramId(), UserState.DECKMENU);
        List<Deck> result = deckRepository.findByOwner(user);
        sendMessageRequest = General.onDeckMenuChoosen(message, user, language, result);
      }
    }
    return Arrays.asList(sendMessageRequest);
  }

  private List<SendMessage> messageOnDeckMenu(Message message, User user, String language,
      UserState state) {
    List<SendMessage> forExecute = null;

    switch (state) {
      case CREATE_DECK_NAME:
        forExecute = createDeck(message, user);
        break;
      case DELETE_DECK_NAME:
        forExecute = deleteDeck(message, user);
        break;
      case VIEW_DECK_NAME:
        forExecute = viewDeck(message, user);
        break;
      case DECKMENU:
        if (message.getText().equals(General.getCreateDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.CREATE_DECK_NAME);
          forExecute = General.onCreateDeckChoosen(message, user, language);
        } else if (message.getText().equals(General.getDeleteDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_DECK_NAME);
          forExecute = General.onDeleteDeckChoosen(message, user, language);
        } else if (message.getText().equals(General.getExploreDeckCommand(language))) {
          userState.put(user.getTelegramId(), UserState.VIEW_DECK_NAME);
          forExecute = General.onViewDeckChoosen(message, user, language);
        }
        break;
      default:
        break;
    }
    return forExecute;
  }

  private List<SendMessage> messageOnViewDeckMenu(Message message, User user, String language,
      UserState state) {
    List<SendMessage> forExecute = null;

    switch (state) {
      case CREATE_FRONT_CARD_NAME:
        forExecute = createCard(message, user);
        break;
      case CREATE_BACK_CARD_NAME:
        forExecute = createCard(message, user);
        break;
      case DELETE_CARD_NAME:
        forExecute = deleteCard(message, user);
        break;
      case CARDMENU:
        if (message.getText().equals(General.getCreateCardCommand(language))) {
          userState.put(user.getTelegramId(), UserState.CREATE_FRONT_CARD_NAME);
          forExecute = General.onCreateCardChoosen(message, user, language);
        } else if (message.getText().equals(General.getDeleteCardCommand(language))) {
          userState.put(user.getTelegramId(), UserState.DELETE_DECK_NAME);
          forExecute = General.onDeleteCardChoosen(message, user, language);
        }
        break;
      default:
        break;
    }
    return forExecute;
  }

  private List<SendMessage> createCard(Message message, User user) {

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());

    if (userState.get(user.getTelegramId()).equals(UserState.CREATE_FRONT_CARD_NAME)) {
      Card card = context.getBean(Card.class);
      card.setFront(message.getText());
      card.setAddingTime(General.getUnixTimeInSeconds());
      card.setDeck(userDeckState.get(user.getTelegramId()));
      cardBuffer.put(user.getTelegramId(), card);
      sendMessage.setText("ENETER BACK CARD NAME");
      userState.put(user.getTelegramId(), UserState.CREATE_BACK_CARD_NAME);
    } else if (userState.get(user.getTelegramId()).equals(UserState.CREATE_BACK_CARD_NAME)) {
      Card card = cardBuffer.get(user.getTelegramId());
      card.setBack(message.getText());
      cardRepository.save(card);
      sendMessage.setText("SUCCESS");
      userState.put(user.getTelegramId(), UserState.CARDMENU);
    }
    return Arrays.asList(sendMessage);
  }

  private List<SendMessage> deleteCard(Message message, User user) {
    Card card = context.getBean(Card.class);

    return null;
  }

  private List<SendMessage> createDeck(Message message, User owner) {
    Deck deck = context.getBean(Deck.class);
    deck.setName(message.getText());
    deck.setOwner(owner);
    deckRepository.save(deck);

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(owner.getTelegramId());
    sendMessage.setText("SUCCESS");

    return Arrays.asList(sendMessage);
  }

  private List<SendMessage> deleteDeck(Message message, User owner) {
    deckRepository.removeByNameAndOwner(message.getText(), owner);
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(owner.getTelegramId());
    sendMessage.setText("SUCCESS");
    return Arrays.asList(sendMessage);
  }
  
  private List<SendMessage> viewDeck(Message message, User user) {
    Deck deck = deckRepository.findByOwnerAndName(user, message.getText());
    userDeckState.put(user.getTelegramId(), deck);
    userState.put(user.getTelegramId(), UserState.CARDMENU);
    List<Card> cards = cardRepository.findByDeck(deck);

    String text = "";

    for (Card card : cards) {
      text = text + card.getFront() + " : " + card.getBack() + "\n";
    }

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(text.isEmpty() ? "EMPTY" : text);
    sendMessage.setReplyMarkup(General.getViewDeckKeyboard("s"));
    return Arrays.asList(sendMessage);
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
