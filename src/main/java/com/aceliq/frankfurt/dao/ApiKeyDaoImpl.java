package com.aceliq.frankfurt.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyDaoImpl implements ApiKeyDao {

  JdbcTemplate jdbcTemplate;

  public ApiKeyDaoImpl(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public Optional<String> getKeyByTelegramId(long telegramId) {
    return jdbcTemplate.query("SELECT api_key FROM api_keys WHERE telegram_id = ?",
        new ResultSetExtractor<Optional<String>>() {
          @Override
          public Optional<String> extractData(ResultSet rs)
              throws SQLException, DataAccessException {
            return Optional.ofNullable(rs.next() ? rs.getString(1) : null);
          }
        }, telegramId);
  }
}
