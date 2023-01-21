package com.aceliq.frankfurt.dao;

import java.util.List;
import java.util.Optional;

public interface ApiKeyDAO {
  
  String getKeyByTelegramId(long telegramId);

}
