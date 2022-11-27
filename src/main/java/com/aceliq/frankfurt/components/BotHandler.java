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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
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
  private HashMap<Long, Pair<String, String>> wordBuffer = new HashMap<Long, Pair<String, String>>();
  private HashMap<Long, List<ScheduledFuture<?>>> futureTask =
      new HashMap<Long, List<ScheduledFuture<?>>>();
  
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
    if (update.hasMessage()) {
      Message message = update.getMessage();
      if (isCommand(message)) {
        handleIncomingCommand(message);
      } else {
        handleIncomingMessage(message);
      }
    }
  }

  public void setLearnTable(long telegramId) {

    threadPoolTaskScheduler.initialize();
    
    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    futureTask.put(telegramId, new ArrayList<ScheduledFuture<?>>());

    for (int i = 1; i < 12; i++) {
      Word word = learnWords.getRandomWord(telegramId);

      date.set(Calendar.SECOND, General.getRandomNumber(1, 57));
      //date.set(Calendar.MINUTE, General.getRandomNumber(1, 57));
      //date.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY) + 1);
 
      Runnable task = new Runnable() {
        @Override
        public void run() {
          String message = word.getForeignWord() + " - " + word.getNativeWord();
          sendMessage(2, telegramId, message, "");
        }
      };
      futureTask.get(telegramId).add(threadPoolTaskScheduler.schedule(task, date.toInstant()));
    }
    sendMessage(1, telegramId, "we_add_all_words", "");
  }

  public void handleIncomingMessage(Message message) {

    long telegramId = message.getFrom().getId();
    String messageText = message.getText();
    resourceBundle = getResourceBundleForUser(telegramId);

    switch (userState.getOrDefault(telegramId, UserState.NONE)) {
      case FOREIGN_WORD:
        wordBuffer.put(telegramId, Pair.of(messageText, ""));
        userState.put(telegramId, UserState.NATIVE_WORD);
        sendMessage(1, telegramId, "enter_native_word", "");
        break;
      case NATIVE_WORD:
        wordBuffer.put(telegramId, Pair.of(wordBuffer.get(telegramId).getFirst(), messageText));
        userState.put(telegramId, UserState.FOREIGN_WORD);
        sendMessage(1, telegramId, "added", "");
        sendMessage(1, telegramId, "enter_foreign_word", "");
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
      default:
        break;
    }
  }
  
  public void handleIncomingCommand(Message message) {
    long telegramId = message.getFrom().getId();
    switch(message.getText()) {
      case "/start":
        createUser(telegramId);
        sendMessage(1, telegramId, "start", "");
        break;
      case "/add":
        userState.put(telegramId, UserState.FOREIGN_WORD);
        sendMessage(1, telegramId, "enter_words", "");
        sendMessage(1, telegramId, "enter_foreign_word", "");
        break;
      case "/vocabulary":
        LinkedList<Word> todayWords = learnWords.getVocabulary(message.getFrom().getId());
        if (todayWords.size() != 0)
          sendMessage(2, telegramId, "", General.convertListOfWordsToString(todayWords));
        else
          sendMessage(1, telegramId, "empty", "");
        break;
      case "/learn_todays_words":
        userState.put(telegramId, UserState.LEARNWORDS);
        learnWords.startLearn(telegramId);
        String nativeWord = learnWords.getWord(telegramId).getNativeWord();
        sendMessage(1, telegramId, "lets_start", "");
        sendMessage(2, telegramId, "", nativeWord);
        learnWords.startLearn(telegramId);
        break;
      case "/finish_add_words":
        setLearnTable(telegramId); 
        break;
    }
  }
  
  public void addWordToVocabulary(long telegramId) {
    Word word = context.getBean(Word.class);
    word.setForeignWord(wordBuffer.get(telegramId).getFirst());
    word.setNativeWord(wordBuffer.get(telegramId).getSecond());
    word.setTelegramId(telegramId);
    word.setAddingTime(General.getUnixTimeInSeconds());
    wordRepository.save(word);
  }

  public boolean isCommand(Message message) {
    return message.getText().matches("^/.*");
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
      sendMessage.setText(additionalText);
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
