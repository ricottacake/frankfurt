package com.aceliq.frankfurt.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
  
  @Id
  private long telegramId;
  private long joinDate;
  private String language;

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public long getTelegramId() {
    return telegramId;
  }

  public void setTelegramId(long telegramId) {
    this.telegramId = telegramId;
  }

  public long getJoinDate() {
    return joinDate;
  }

  public void setJoinDate(long joinDate) {
    this.joinDate = joinDate;
  }
}
