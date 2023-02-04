package com.aceliq.frankfurt.database;

import java.util.Optional;
import com.aceliq.frankfurt.models.User;

public interface UserDao {
  Optional<User> findById(long telegramId);
  void save(User user);
}
