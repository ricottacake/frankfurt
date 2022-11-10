package com.aceliq.frankfurt.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "words")
public class Word {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String foreignWord;
  private String nativeWord;
  private long addingTime;
  private long telegramId;

  public void setAddingTime(long addingTime) {
    this.addingTime = addingTime;
  }

  public long getTelegramId() {
    return telegramId;
  }

  public void setTelegramId(long telegramId) {
    this.telegramId = telegramId;
  }

  public long getAddingTime() {
    return addingTime;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getForeignWord() {
    return foreignWord;
  }

  public void setForeignWord(String foreignWord) {
    this.foreignWord = foreignWord;
  }

  public String getNativeWord() {
    return nativeWord;
  }

  public void setNativeWord(String nativeWord) {
    this.nativeWord = nativeWord;
  }
}
