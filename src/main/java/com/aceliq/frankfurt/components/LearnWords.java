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
    map.put(telegramId, getVocabulary(telegramId));
  }

  public Word getWord(long telegramId) {
    return map.get(telegramId).getFirst();
  }

  public LinkedList<Word> getVocabulary(long telegramId) {
    
    return new LinkedList<Word>(
        wordRepository.findByTelegramId(telegramId));
  }

  public Word getRandomWord(long telegramId) {
    LinkedList<Word> words = getVocabulary(telegramId);
    Word word = words.get(getRandomNumber(0, words.size()));
    return word;
  }
}
