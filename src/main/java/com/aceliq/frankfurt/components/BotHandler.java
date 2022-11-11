package com.aceliq.frankfurt.components;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
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
import com.aceliq.frankfurt.util.General;

@Component
public class BotHandler extends TelegramLongPollingBot {
  
  private Locale locale;
  private ResourceBundle resourceBundle;

  private HashMap<Long, UserState> userState = new HashMap<Long, UserState>();
  private HashMap<Long, List<ScheduledFuture<?>>> futureTask =
      new HashMap<Long, List<ScheduledFuture<?>>>();
  private String timezonePattern = "^[-]?\\d{1,2}";
  
  @Value("${TELEGRAM_BOT_KEY}")
  private String botToken;
  
  @Value("${TELEGRAM_BOT_USERNAME}")
  private String botUsername;

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
  
  public ResourceBundle getResourceBundleForUser(long telegramId) {
    
    Optional<User> user = usersRepository.findById(telegramId);
    String lang = user.get().getLang();
    
    locale = new Locale(lang.split("_")[0], lang.split("_")[1]);
    resourceBundle = ResourceBundle.getBundle("bundle", locale);
    
    return resourceBundle;
  }

  @Override
  public void onUpdateReceived(Update update) {
    
    try {
      
      if (update.hasMessage()) {
        Message message = update.getMessage();

        if (isCommand(message)) {
          return;
        }

        if (message.hasText()) {
          handleIncomingMessage(message);
        }
      }
    } catch (Exception e) {

    }
  }

  public void setLearnTable(long telegramId) {

    threadPoolTaskScheduler.initialize();

    String userTimeZone = usersRepository.findById(telegramId).get().getTimezone();
    TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
    Calendar date = Calendar.getInstance(timeZone);

    int timeDifferent = 21 - date.get(Calendar.HOUR_OF_DAY);

    if (timeDifferent < 2) {
      sendMessage(1, telegramId, "sorry", "");
      return;
    }

    futureTask.put(telegramId, new ArrayList<ScheduledFuture<?>>());

    for (int i = 1; i < timeDifferent; i++) {
      Word word = learnWords.getTodaysRandomWord(telegramId);

      date.set(Calendar.MINUTE, General.getRandomNumber(1, 57));
      date.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY) + 1);

      Runnable task = new Runnable() {
        @Override
        public void run() {
          String message = word.getForeignWord() + " - " + word.getNativeWord();
          sendMessage(2, telegramId, "", message);
        }
      };
      futureTask.get(telegramId).add(threadPoolTaskScheduler.schedule(task, date.toInstant()));
    }
    sendMessage(1, telegramId, "we_add_all_words", "");
  }

  public void handleIncomingMessage(Message message) throws TelegramApiException {

    long telegramId = message.getFrom().getId();
    String messageText = message.getText();
    resourceBundle = getResourceBundleForUser(telegramId);

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
        word.setAddingTime(General.getUnixTimeInSeconds());
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
          sendMessage(1, telegramId, "right", "");
        else {
          sendMessage(1, telegramId, "mistake", "");
          sendMessage(3, telegramId, "right_answer_is", learnWords.getWord(telegramId).getForeignWord());
          
        }
        learnWords.removeFirst(telegramId);
        if (learnWords.getCountWords(telegramId) == 0) {
          sendMessage(1, telegramId, "thats_all_good_work", "");
          return;
        }
        String nextWord = learnWords.getWord(telegramId).getNativeWord();
        sendMessage(2, telegramId, "", nextWord);
        break;
    }
  }

  public boolean isCommand(Message message) {

    long telegramId = message.getFrom().getId();

    if (message.getText().equals("/start")) {
      createUser(telegramId);
      sendMessage(1, telegramId, "start", "");
      userState.put(telegramId, UserState.REGTIMEZONE);
      return true;
    }

    if (message.getText().equals("/add_words")) {
      sendMessage(1, telegramId, "enter_words", "");
      userState.put(telegramId, UserState.ADD_WORDS);
      return true;
    }

    if (message.getText().equals("/todays_vocabulary")) {
      LinkedList<Word> todayWords = learnWords.getTodaysWords(message.getFrom().getId());
      if (todayWords.size() != 0)
        sendMessage(2, telegramId, "", General.convertListOfWordsToString(todayWords));
      else
        sendMessage(1, telegramId, "you_not_added", "");
      return true;
    }

    if (message.getText().equals("/learn_todays_words")) {
      userState.put(telegramId, UserState.LEARNWORDS);
      learnWords.startLearn(telegramId);
      String nativeWord = learnWords.getWord(telegramId).getNativeWord();
      sendMessage(1, telegramId, "lets_start", "");
      sendMessage(2, telegramId, "", nativeWord);
      learnWords.startLearn(telegramId);
      return true;
    }

    if (message.getText().equals("/finish_add_words")) {
      setLearnTable(telegramId);
      return true;
    }
    return false;
  }

  public void createUser(long telegramId) {
    User user = context.getBean(User.class);
    user.setTelegramId(telegramId);
    user.setJoinDate(General.getUnixTimeInSeconds());
    user.setTimezone("GMT+3:00");
    usersRepository.save(user);
  }

  public void sendMessage(int type, long telegramId, String text, String additionalText) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(telegramId);
    
    if(type == 1) {
      resourceBundle = getResourceBundleForUser(telegramId);
      sendMessage.setText(resourceBundle.getString(text));
    } else if(type == 2) {
      sendMessage.setText(text);
    } else if(type == 3) {
      String s = resourceBundle.getString(text) + additionalText;
      sendMessage.setText(s);
    }
    try {
      execute(sendMessage);
    } catch (TelegramApiException e) {
      e.printStackTrace();
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
