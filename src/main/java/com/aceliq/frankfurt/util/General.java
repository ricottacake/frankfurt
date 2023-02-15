package com.aceliq.frankfurt.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.User;

public class General {

  public static long getUnixTimeInSeconds() {
    TimeZone timeZone = TimeZone.getTimeZone("GMT");
    Calendar startDay = Calendar.getInstance(timeZone);
    return startDay.getTimeInMillis() / 1000L;
  }

  public static int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  /***********************************************************************************************************/

  public static String getGoToMenu(String language) {
    return String.format(LocalisationService.getString("you_are_in_main_menu", language));
  }

  public static String getStartResponse(String language) {
    return String.format(LocalisationService.getString("start_message", language));
  }

  public static String getMyDecksCommand(String language) {
    return String.format(LocalisationService.getString("my_decks", language));
  }
  
  public static String getBackCommand(String language) {
    return String.format(LocalisationService.getString("back", language));
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

  public static String getCreateDeckCommandResponse(String language) {
    return String.format(LocalisationService.getString("enter_name_for_new_deck", language));
  }

  public static String getViewDeckCommandResponse(String language) {
    return String.format(LocalisationService.getString("enter_name_for_view_deck", language));
  }

  public static String getDeleteDeckCommandResponse(String language) {
    return String.format(LocalisationService.getString("enter_name_for_delete_deck", language));
  }

  public static String getSuccessResponse(String language) {
    return String.format(LocalisationService.getString("success", language));
  }

  public static String getMenuCommand(String language) {
    return String.format(LocalisationService.getString("menu", language));
  }

  public static String getEnterBackCardNameResponse(String language) {
    return String.format(LocalisationService.getString("enter_back_card_message", language));
  }

  public static String getDeckExistResponse(String language) {
    return String.format(LocalisationService.getString("deck_exist", language));
  }

  public static String getDeckNotExistResponse(String language) {
    return String.format(LocalisationService.getString("deck_not_exist", language));
  }

  public static String getEnterCardSequenceNumber(String language) {
    return String.format(LocalisationService.getString("enter_card_sequence_number", language));
  }

  public static String getMessageThereIsNothingHere(String language) {
    return String.format(LocalisationService.getString("there_is_nothing_here", language));
  }

  /***********************************************************************************************************/

  public static List<SendMessage> onCreateDeckChoosen(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(getCreateDeckCommandResponse(user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onViewDeckChoosen(User user, List<Deck> decks) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(getViewDeckCommandResponse(user.getLanguage()));
    sendMessage.setReplyMarkup(getDeckOptionsKeyboard(decks));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onDeleteDeckChoosen(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(getDeleteDeckCommandResponse(user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  /***********************************************************************************************************/

  public static List<SendMessage> onCreateCardChoosen(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(LocalisationService.getString("enter_front_name", user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  public static List<SendMessage> onDeleteCardChoosen(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(getEnterCardSequenceNumber(user.getLanguage()));
    return Arrays.asList(sendMessage);
  }

  /***********************************************************************************************************/

  public static ReplyKeyboardMarkup getDeckOptionsKeyboard(List<Deck> options) {

    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();

    var wrapper = new Object() {
      KeyboardRow row = new KeyboardRow();
    };

    IntStream.range(0, options.size()).boxed().peek(e -> {
      if (e % 3 == 0) {
        keyboard.add(wrapper.row);
        wrapper.row = new KeyboardRow();
      }
      wrapper.row.add(new KeyboardButton(options.get(e).getName()));
    }).collect(Collectors.toList());
    keyboard.add(wrapper.row);
    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  public static ReplyKeyboardMarkup getQuestionOptionsKeyboard(List<Card> options) {

    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();

    KeyboardRow row1 = new KeyboardRow();
    KeyboardRow row2 = new KeyboardRow();

    KeyboardButton option0 = new KeyboardButton();
    option0.setText(options.get(0).getBack());

    KeyboardButton option1 = new KeyboardButton();
    option1.setText(options.get(1).getBack());


    row1.add(option0);
    row1.add(option1);

    KeyboardButton option2 = new KeyboardButton();
    option2.setText(options.get(2).getBack());

    KeyboardButton option3 = new KeyboardButton();
    option3.setText(options.get(3).getBack());

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
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();
    
    KeyboardRow row1 = new KeyboardRow();

    KeyboardButton createDeck = new KeyboardButton();
    createDeck.setText(General.getCreateDeckCommand(language));

    KeyboardButton deleteDeck = new KeyboardButton();
    deleteDeck.setText(General.getDeleteDeckCommand(language));

    KeyboardButton exploreDeck = new KeyboardButton();
    exploreDeck.setText(General.getExploreDeckCommand(language));

    KeyboardButton menu = new KeyboardButton();
    menu.setText(General.getMenuCommand(language));

    row1.add(createDeck);
    row1.add(deleteDeck);
    row1.add(exploreDeck);
    row1.add(menu);
    
    KeyboardRow row2 = new KeyboardRow();
    
    KeyboardButton back = new KeyboardButton();
    back.setText(General.getBackCommand(language));
    
    row2.add(back);

    keyboard.add(row1);
    keyboard.add(row2);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  public static ReplyKeyboardMarkup getExploreDeckKeyboard(String language) {
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(true);
    keyboardMarkup.setSelective(true);

    List<KeyboardRow> keyboard = new ArrayList<>();
    
    KeyboardRow row1 = new KeyboardRow();

    KeyboardButton createCard = new KeyboardButton();
    createCard.setText(General.getCreateCardCommand(language));

    KeyboardButton deleteCard = new KeyboardButton();
    deleteCard.setText(General.getDeleteCardCommand(language));

    KeyboardButton studyDeck = new KeyboardButton();
    studyDeck.setText(General.getStudyDeckCommand(language));

    KeyboardButton menu = new KeyboardButton();
    menu.setText(General.getMenuCommand(language));

    row1.add(createCard);
    row1.add(deleteCard);
    row1.add(studyDeck);
    row1.add(menu);
    
    KeyboardRow row2 = new KeyboardRow();
    
    KeyboardButton back = new KeyboardButton();
    back.setText(General.getBackCommand(language));
    
    row2.add(back);

    keyboard.add(row1);
    keyboard.add(row2);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  public static ReplyKeyboardMarkup getMainMenuKeyboard(String language) {
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
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

  /***********************************************************************************************************/

  public static SendMessage getDeckListMessage(User user, List<Card> cards) {

    String x = IntStream.range(1, cards.size() + 1)
        .mapToObj(
            i -> i + "\\. " + cards.get(i - 1).getFront() + " : " + cards.get(i - 1).getBack())
        .collect(Collectors.joining("\n"));

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setParseMode("MarkdownV2");
    sendMessage.setText(x.isEmpty() ? (getMessageThereIsNothingHere(user.getLanguage())) : x);
    sendMessage.setReplyMarkup(getExploreDeckKeyboard("en"));
    return sendMessage;
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
    sendMessage.setText(getDeckExistResponse(user.getLanguage()));
    return sendMessage;
  }

  public static SendMessage getDeckNotExistMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(getDeckNotExistResponse(user.getLanguage()));
    return sendMessage;
  }

  public static SendMessage getEnterBackCardNameMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(getEnterBackCardNameResponse(user.getLanguage()));
    return sendMessage;
  }

  public static SendMessage getQuestionMessage(User user, String word, List<Card> options) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setReplyMarkup(getQuestionOptionsKeyboard(options));
    sendMessage.setText(word);
    return sendMessage;
  }

  public static SendMessage getOnMenuMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage()));
    sendMessage.setText(getGoToMenu(user.getLanguage()));
    return sendMessage;
  }

  public static SendMessage getMyDeckMessage(User user, List<Deck> result) {
    ReplyKeyboardMarkup replyKeyboardMarkup = General.getDeckMenuKeyboard(user.getLanguage());

    String x = IntStream.range(1, result.size() + 1)
        .mapToObj(i -> i + "\\. " + result.get(i - 1).getName()).collect(Collectors.joining("\n"));

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setParseMode("MarkdownV2");
    sendMessage.setText(x.isEmpty() ? (getMessageThereIsNothingHere(user.getLanguage())) : x);
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    return sendMessage;
  }

  public static SendMessage getStartMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText(getStartResponse(user.getLanguage()));
    sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().toString()));
    return sendMessage;
  }

  public static SendMessage getRightMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText("Right");
    sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().toString()));
    return sendMessage;
  }

  public static SendMessage getNoRightMessage(User user) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(user.getTelegramId());
    sendMessage.setText("No Right.");
    sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().toString()));
    return sendMessage;
  }

  /***********************************************************************************************************/

}
