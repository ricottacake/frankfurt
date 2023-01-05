package com.aceliq.frankfurt.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.User;

public class General {

  public static String convertListOfWordsToString(List<Card> listOfWords) {
    String result = "";
    for (int i = 0; i < listOfWords.size(); i++)
      result = result + listOfWords.get(i).getBack() + " - " + listOfWords.get(i).getFront() + "\n";
    return result;
  }

  public static long getUnixTimeInSeconds() {
    TimeZone timeZone = TimeZone.getTimeZone("GMT");
    Calendar startDay = Calendar.getInstance(timeZone);
    return startDay.getTimeInMillis() / 1000L;
  }

  public static int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  public static String getCreateDeckCommand(String language) {
    return String.format(LocalisationService.getString("create_deck_command", language));
  }

  public static String getDeleteDeckCommand(String language) {
    return String.format(LocalisationService.getString("delete_deck_command", language));
  }

  public static String getCreateCardCommand(String language) {
    return String.format(LocalisationService.getString("create_card_command", language));
  }

  public static String getDeleteCardCommand(String language) {
    return String.format(LocalisationService.getString("delete_card_command", language));
  }

  public static String getStudyDeckCommand(String language) {
    return String.format(LocalisationService.getString("study_deck_command", language));
  }

  public static String getExploreDeckCommand(String language) {
    return String.format(LocalisationService.getString("explore_deck_command", language));
  }

  public static List<SendMessage> onCreateDeckChoosen(Message message, User user, String language) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText(LocalisationService.getString("enter_name_for_new_deck", language));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onViewDeckChoosen(Message message, User user, String language) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText(LocalisationService.getString("enter_name_for_view_deck", language));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onDeleteDeckChoosen(Message message, User user, String language) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText(LocalisationService.getString("enter_name_for_delete_deck", language));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onCreateCardChoosen(Message message, User user, String language) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText("ENTER NEW CARD FRONT NAME:");
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onDeleteCardChoosen(Message message, User user, String language) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText("ENTER DECK NAME FOR DELETE:");
    return Arrays.asList(sendMessage);
  }

  public static SendMessage onStart(Message message, User user, String language) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText("Hello!\n"
        + "This bot will help you learn foreign words with the help of flashcards. You can create a large number of decks of cards and add cards to them with a specific phrase and translation into your own language. In addition, the bot allows you to check how well you learned this deck of cards, to select a deck and click \"Check me!\" the number of correct answers.");
    sendMessage.setReplyMarkup(getMainMenuKeyboard(language));
    return sendMessage;
  }

  public static ReplyKeyboardMarkup getMainMenuKeyboard(String language) {
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(true);
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row = new KeyboardRow();

    KeyboardButton myDecks = new KeyboardButton();
    myDecks.setText("My Decks");

    KeyboardButton settings = new KeyboardButton();
    settings.setText("Settings");

    row.add(myDecks);
    row.add(settings);

    keyboard.add(row);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  public static SendMessage onBackMenuChoosen(Message message, User user, String language) {
    ReplyKeyboardMarkup replyKeyboardMarkup = getMainMenuKeyboard(language);
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    sendMessage.setText("You are in Main Menu.");
    return sendMessage;
  }

  public static ReplyKeyboardMarkup getDeckMenuKeyboard(String language) {

    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(true);
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row = new KeyboardRow();

    KeyboardButton createDeck = new KeyboardButton();
    createDeck.setText(General.getCreateDeckCommand(language));

    KeyboardButton deleteDeck = new KeyboardButton();
    deleteDeck.setText(General.getDeleteDeckCommand(language));

    KeyboardButton exploreDeck = new KeyboardButton();
    exploreDeck.setText(General.getExploreDeckCommand(language));

    KeyboardButton menu = new KeyboardButton();
    menu.setText("MENU");

    row.add(createDeck);
    row.add(deleteDeck);
    row.add(exploreDeck);
    row.add(menu);

    keyboard.add(row);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  public static ReplyKeyboardMarkup getExploreDeckKeyboard(String language) {
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(true);
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row = new KeyboardRow();

    KeyboardButton createCard = new KeyboardButton();
    createCard.setText(General.getCreateCardCommand(language));

    KeyboardButton deleteCard = new KeyboardButton();
    deleteCard.setText(General.getDeleteCardCommand(language));

    KeyboardButton studyDeck = new KeyboardButton();
    studyDeck.setText(General.getStudyDeckCommand(language));

    KeyboardButton menu = new KeyboardButton();
    menu.setText("MENU");

    row.add(createCard);
    row.add(deleteCard);
    row.add(studyDeck);
    row.add(menu);

    keyboard.add(row);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  public static String getMyDeckCommand(String language) {
    return String.format(LocalisationService.getString("my_deck", language));
  }

  public static boolean isCommandForOther(String text) {
    boolean isSimpleCommand = text.equals("/start") || text.equals("/help") || text.equals("/stop");
    boolean isCommandForMe = text.equals("/start@weatherbot") || text.equals("/help@weatherbot")
        || text.equals("/stop@weatherbot");
    return text.startsWith("/") && !isSimpleCommand && !isCommandForMe;
  }

  public static SendMessage onDeckMenuChoosen(Message message, User user, String language,
      List<Deck> result) {
    ReplyKeyboardMarkup replyKeyboardMarkup = General.getDeckMenuKeyboard(language);

    String text = "";
    for (Deck d : result)
      text = text + d.getName() + "\n";

    if (result.isEmpty()) {
      text = "EMPTY";
    }

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText(text);
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    return sendMessage;
  }
  
  public static SendMessage convertDeckListToMessage(List<Card> cards, User user) {
    String text = "";
    for (Card card : cards)
      text = text + card.getFront() + " : " + card.getBack() + "\n";
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(text.isEmpty() ? "EMPTY" : text);
    return sendMessage;
  }
}
