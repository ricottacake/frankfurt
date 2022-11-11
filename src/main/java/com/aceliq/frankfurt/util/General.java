package com.aceliq.frankfurt.util;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import com.aceliq.frankfurt.models.Word;

public class General {
  
  public static String convertListOfWordsToString(List<Word> listOfWords) {
    String result = "";
    for (int i = 0; i < listOfWords.size(); i++)
      result = result + listOfWords.get(i).getForeignWord() + " - "
          + listOfWords.get(i).getNativeWord() + "\n";
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

}
