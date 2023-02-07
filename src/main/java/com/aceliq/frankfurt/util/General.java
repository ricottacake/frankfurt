package com.aceliq.frankfurt.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  public static String getMyDecksCommand(String language) {
    return String.format(LocalisationService.getString("my_decks", language));
  }

  public static String getSettingsCommand(String language) {
    return String.format(LocalisationService.getString("settings", language));
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

  public static String getMenuCommand(String language) {
    return String.format(LocalisationService.getString("menu", language));
  }

  public static SendMessage getSuccessMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(LocalisationService.getString("success", user.getLanguage()));
    return sendMessage;
  }

  public static SendMessage getDeckExistMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(LocalisationService.getString("deck_exist", user.getLanguage()));
    return sendMessage;
  }

  public static SendMessage getDeckNotExistMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(LocalisationService.getString("deck_not_exist", user.getLanguage()));
    return sendMessage;
  }

  public static SendMessage getEnterBackCardNameMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage
        .setText(LocalisationService.getString("enter_back_card_message", user.getLanguage()));
    return sendMessage;
  }
  
  public static String getRandomBackNameFromList(List<Card> cards) {
    int size = cards.size();
    int random = getRandomNumber(0, size);
    Card card = cards.get(random);
    return card.getBack();
  }
  
  public static SendMessage getQuestionMessage(User user, String word, String[] options) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setReplyMarkup(getQuestionOptionsKeyboard(options));
    sendMessage.setText(word);
    return sendMessage;
  }

  public static List<SendMessage> onCreateDeckChoosen(Message message, User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage
        .setText(LocalisationService.getString("enter_name_for_new_deck", user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onViewDeckChoosen(Message message, User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage
        .setText(LocalisationService.getString("enter_name_for_view_deck", user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onDeleteDeckChoosen(Message message, User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage
        .setText(LocalisationService.getString("enter_name_for_delete_deck", user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onCreateCardChoosen(Message message, User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText(LocalisationService.getString("enter_front_name", user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onDeleteCardChoosen(Message message, User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText(LocalisationService.getString("enter_card_sequence_number", user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  public static SendMessage onStart(Message message, User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText("Hello!\n"
        + "This bot will help you learn foreign words with the help of flashcards. You can create a large number of decks of cards and add cards to them with a specific phrase and translation into your own language. In addition, the bot allows you to check how well you learned this deck of cards, to select a deck and click \"Check me!\" the number of correct answers.");
    sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().toString()));
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
    myDecks.setText(getMyDecksCommand(language));

    KeyboardButton settings = new KeyboardButton();
    settings.setText(getSettingsCommand(language));

    row.add(myDecks);
    row.add(settings);

    keyboard.add(row);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  public static SendMessage onBackMenuChoosen(Message message, User user) {
    ReplyKeyboardMarkup replyKeyboardMarkup = getMainMenuKeyboard(user.getLanguage());
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    sendMessage.setText(LocalisationService.getString("you_are_in_main_menu", user.getLanguage()));
    return sendMessage;
  }
  
  public static ReplyKeyboardMarkup getQuestionOptionsKeyboard(String[] options) {

    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();
    
    KeyboardRow row1 = new KeyboardRow();
    KeyboardRow row2 = new KeyboardRow();

    KeyboardButton option0 = new KeyboardButton();
    option0.setText(options[0]);

    KeyboardButton option1 = new KeyboardButton();
    option1.setText(options[1]);
    
    row1.add(option0);
    row1.add(option1);

    KeyboardButton option2 = new KeyboardButton();
    option2.setText(options[2]);

    KeyboardButton option3 = new KeyboardButton();
    option3.setText(options[3]);

    row2.add(option2);
    row2.add(option3);

    keyboard.add(row1);
    keyboard.add(row2);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
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
    menu.setText(General.getMenuCommand(language));

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
    menu.setText(General.getMenuCommand(language));

    row.add(createCard);
    row.add(deleteCard);
    row.add(studyDeck);
    row.add(menu);

    keyboard.add(row);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
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

    String x = IntStream.range(1, result.size() + 1)
        .mapToObj(i -> i + "\\. " + result.get(i - 1).getName())
        .collect(Collectors.joining("\n"));

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setParseMode("MarkdownV2");
    sendMessage.setText(x.isEmpty() ? (LocalisationService.getString("there_is_nothing_here", user.getLanguage())) : x);
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    return sendMessage;
  }

  public static SendMessage convertDeckListToMessage(List<Card> cards, User user) {

    String x = IntStream.range(1, cards.size() + 1)
        .mapToObj(i -> i + "\\. " + cards.get(i - 1).getFront() + " : " + cards.get(i - 1).getBack())
        .collect(Collectors.joining("\n"));

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setParseMode("MarkdownV2");
    sendMessage.setText(x.isEmpty() ? (LocalisationService.getString("there_is_nothing_here", user.getLanguage())) : x);
    sendMessage.setReplyMarkup(getExploreDeckKeyboard("en"));
    return sendMessage;
  }
  
}
