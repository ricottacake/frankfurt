package com.aceliq.frankfurt.dao;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApiKey implements ApiKeyDAO {

  JdbcTemplate jdbcTemplate;

  public ApiKey(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public String getKeyByTelegramId(long telegramId) {
    return jdbcTemplate.queryForObject("SELECT api_key FROM api_keys WHERE telegram_id = ?",
        String.class, telegramId);
  }
}
