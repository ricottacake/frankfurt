package com.aceliq.frankfurt.components;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.aceliq.frankfurt.database.UsersRepository;
import com.aceliq.frankfurt.database.WordRepository;
import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.models.UserState;
import com.aceliq.frankfurt.models.Word;

@Component
public class BotHandler extends TelegramLongPollingBot {

  private HashMap<Long, UserState> userState = new HashMap<Long, UserState>();
  private HashMap<Long, List<ScheduledFuture<?>>> futureTask =
      new HashMap<Long, List<ScheduledFuture<?>>>();

  final private String A1 =
      "Hello! This bot will help you learn foreign words. For example, you have a habit of learning words every day by reading various newspapers or magazines such as the New York Times. Add new words to this bot, and then, during the day, we will send you a new word every hour along with the translation, and you will try to memorize them. At the end of the day you will have the opportunity to take a short test to check how well you remember the words.";
  final private String W1 =
      "Perfect! Enter Please your words in format word - translate. When in finish just press button Finish.";
  final private String W2 = "You not added words yet.";
  final private String W3 = "Sorry, too late. See you tomorrow!";
  final private String W4 = "Lets Start!";
  final private String W5 = "Finish!!";
  final private String W6 = "Right!";
  final private String W7 = "Mistake";
  final private String W8 = "Perfect! We add all your words, Good Luck in learn!";
  final private String W9 = "Thats all. Good Work!";
  final private String W10 = "Right answer is ";
  
  @Value("${TELEGRAM_BOT_KEY}")
  private String botToken;

  @Autowired
  private LearnWords learnWords;

  @Autowired
  private WordRepository wordRepository;

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private ThreadPoolTaskScheduler threadPoolTaskScheduler;

  @Autowired
  private ApplicationContext context;

  @Override
  public void onUpdateReceived(Update update) {

    try {
      
      if (update.hasMessage()) {
        Message message = update.getMessage();

        if (isCommand(message)) {
          return;
        }

        if (message.hasText() || message.hasLocation()) {
          handleIncomingMessage(message);
        }
      }
    } catch (Exception e) {

    }
  }

  public int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }
  
  public long getUnixTimeInSeconds() {
    TimeZone timeZone = TimeZone.getTimeZone("GMT");
    Calendar startDay = Calendar.getInstance(timeZone);
    return startDay.getTimeInMillis() / 1000L;
  }

  private void setLearnTable(long telegramId) {

    threadPoolTaskScheduler.initialize();

    String userTimeZone = usersRepository.findById(telegramId).get().getTimezone();
    TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
    Calendar date = Calendar.getInstance(timeZone);

    int timeDifferent = 21 - date.get(Calendar.HOUR_OF_DAY);

    if (timeDifferent < 2) {
      sendMessage(W3, telegramId);
      return;
    }

    futureTask.put(telegramId, new ArrayList<ScheduledFuture<?>>());

    for (int i = 1; i < timeDifferent; i++) {
      Word word = learnWords.getTodaysRandomWord(telegramId);

      date.set(Calendar.MINUTE, getRandomNumber(1, 57));
      date.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY) + 1);

      Runnable task = new Runnable() {
        @Override
        public void run() {
          String message = word.getForeignWord() + " - " + word.getNativeWord();
          sendMessage(message, telegramId);
        }
      };
      futureTask.get(telegramId).add(threadPoolTaskScheduler.schedule(task, date.toInstant()));
    }
    sendMessage(W8, telegramId);
  }

  private void handleIncomingMessage(Message message) throws TelegramApiException {

    long telegramId = message.getFrom().getId();
    String messageText = message.getText();
    String timezonePattern = "^[-]?\\d{1,2}";

    switch (userState.get(telegramId)) {
      case MENU:
        break;
      case ADD_WORDS:
        String foreignWord = messageText.split("-")[0].replaceAll("\\s+", "");
        String nativeWord = messageText.split("-")[1].replaceAll("\\s+", "");
        
        Word word = context.getBean(Word.class);
        word.setForeignWord(foreignWord);
        word.setNativeWord(nativeWord);
        word.setTelegramId(telegramId);
        word.setAddingTime(getUnixTimeInSeconds());
        wordRepository.save(word);
        break;
      case REGTIMEZONE:
        boolean valid = Pattern.matches(timezonePattern, messageText);
        if (valid) {
          User user = usersRepository.findById(telegramId).get();
          user.setTimezone(messageText);
          usersRepository.save(user);
        }
        break;
      case LEARNWORDS:
        if (learnWords.checkWord(messageText, telegramId))
          sendMessage(W6, telegramId);
        else {
          sendMessage(W7, telegramId);
          String s = W10 + learnWords.getWord(telegramId).getForeignWord();
          sendMessage(s, telegramId);
        }
        learnWords.removeFirst(telegramId);
        if (learnWords.getCountWords(telegramId) == 0) {
          sendMessage(W9, telegramId);
          return;
        }
        String nextWord = learnWords.getWord(telegramId).getNativeWord();
        sendMessage(nextWord, telegramId);
        break;
    }
  }

  public boolean isCommand(Message message) {

    long telegramId = message.getFrom().getId();

    if (message.getText().equals("/start")) {
      createUser(telegramId);
      sendMessage(A1, telegramId);
      userState.put(telegramId, UserState.REGTIMEZONE);
      return true;
    }

    if (message.getText().equals("/add_words")) {
      sendMessage(W1, message.getFrom().getId());
      userState.put(telegramId, UserState.ADD_WORDS);
      return true;
    }

    if (message.getText().equals("/todays_vocabulary")) {
      LinkedList<Word> todayWords = learnWords.getTodaysWords(message.getFrom().getId());
      if (todayWords.size() != 0)
        sendMessage(convertListOfWordsToString(todayWords), telegramId);
      else
        sendMessage(W2, telegramId);
      return true;
    }

    if (message.getText().equals("/learn_todays_words")) {
      userState.put(telegramId, UserState.LEARNWORDS);
      learnWords.startLearn(telegramId);
      String nativeWord = learnWords.getWord(telegramId).getNativeWord();
      sendMessage(W4, telegramId);
      sendMessage(nativeWord, telegramId);
      learnWords.startLearn(telegramId);
      return true;
    }

    if (message.getText().equals("/finish_add_words")) {
      setLearnTable(telegramId);
      return true;
    }
    return false;
  }

  public String convertListOfWordsToString(List<Word> listOfWords) {
    String result = "";
    for (int i = 0; i < listOfWords.size(); i++)
      result = result + listOfWords.get(i).getForeignWord() + " - "
          + listOfWords.get(i).getNativeWord() + "\n";
    return result;
  }

  public void createUser(long telegramId) {
    User user = context.getBean(User.class);
    user.setTelegramId(telegramId);
    user.setJoinDate(getUnixTimeInSeconds());
    user.setTimezone("GMT+3:00");
    usersRepository.save(user);
  }

  public void sendMessage(String text, long telegramId) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(telegramId);
    sendMessage.setText(text);
    try {
      execute(sendMessage);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotUsername() {
    return "fortalkBot";
  }

  @Override
  public String getBotToken() {
    return botToken;
  }
}
