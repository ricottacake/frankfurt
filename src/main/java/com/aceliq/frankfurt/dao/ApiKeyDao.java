package com.aceliq.frankfurt.dao;

import java.util.Optional;

public interface ApiKeyDao {
  Optional<String> getKeyByTelegramId(long telegramId);
}
