package com.aceliq.frankfurt.components;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.aceliq.frankfurt.database.UserRepository;
import com.aceliq.frankfurt.database.CardRepository;
import com.aceliq.frankfurt.models.Card;

@Component
public class LearnWords {

  private HashMap<Long, ArrayDeque<Card>> map = new HashMap<>();
  
  @Autowired
  private CardRepository wordRepository;

  @Autowired
  private UserRepository usersRepository;

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
    String expectedWord = map.get(telegramId).getFirst().getBack();
    if (word.equals(expectedWord))
      return true;
    return false;
  }
  
  public List<Card> getVocabulary(long telegramId) {
    //return wordRepository.findByTelegramId(telegramId);
    return null;
  }

  public void startLearn(long telegramId) {
    //List<Card> list = wordRepository.findByTelegramId(telegramId);
    //Collections.shuffle(list);
    //map.put(telegramId, new ArrayDeque<Card>(list));
  }

  public Card getWord(long telegramId) {
    return map.get(telegramId).getFirst();
  }
}
