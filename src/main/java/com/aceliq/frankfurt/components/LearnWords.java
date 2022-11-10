package com.aceliq.frankfurt.components;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.aceliq.frankfurt.database.UsersRepository;
import com.aceliq.frankfurt.database.WordRepository;
import com.aceliq.frankfurt.models.Word;

@Component
public class LearnWords {

  private HashMap<Long, LinkedList<Word>> map = new HashMap<>();
  
  @Autowired
  private WordRepository wordRepository;

  @Autowired
  private UsersRepository usersRepository;

  public int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }
  
  public void removeFirst(long telegramId) {
    map.get(telegramId).removeFirst();
  }
  
  public int getCountWords(long telegramId) {
    return map.get(telegramId).size();
  }
  
  public boolean checkWord(String word, long telegramId) {
    String expectedWord = map.get(telegramId).getFirst().getForeignWord();
    if (word.equals(expectedWord))
      return true;
    return false;
  }

  public void startLearn(long telegramId) {
    map.put(telegramId, getTodaysWords(telegramId));
  }

  public Word getWord(long telegramId) {
    return map.get(telegramId).getFirst();
  }

  public LinkedList<Word> getTodaysWords(long telegramId) {
    String userTimeZone = usersRepository.findById(telegramId).get().getTimezone();
    TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);

    Calendar startDay = Calendar.getInstance(timeZone);
    startDay.set(Calendar.HOUR_OF_DAY, 0);
    startDay.set(Calendar.MINUTE, 0);
    startDay.set(Calendar.SECOND, 0);

    Calendar endDay = Calendar.getInstance(timeZone);
    endDay.set(Calendar.HOUR_OF_DAY, 23);
    endDay.set(Calendar.MINUTE, 59);
    endDay.set(Calendar.SECOND, 59);

    long start = startDay.getTimeInMillis() / 1000L;
    long end = endDay.getTimeInMillis() / 1000L;
    
    return new LinkedList<Word>(
        wordRepository.findByTelegramIdAndAddingTimeBetween(telegramId, start, end));
  }

  public Word getTodaysRandomWord(long telegramId) {
    LinkedList<Word> words = getTodaysWords(telegramId);
    Word word = words.get(getRandomNumber(0, words.size()));
    return word;
  }
}
