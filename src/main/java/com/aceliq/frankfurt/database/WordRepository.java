package com.aceliq.frankfurt.database;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.aceliq.frankfurt.models.Word;

@Repository
public interface WordRepository extends CrudRepository<Word, Integer> {
  List<Word> findByTelegramIdAndAddingTimeBetween(long telegramId, long startAddingTime,
      long endAddingTime);
  List<Word> findByTelegramId(long telegramId);
}
