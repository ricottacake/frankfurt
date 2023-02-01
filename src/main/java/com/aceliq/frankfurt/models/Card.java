package com.aceliq.frankfurt.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "cards")
public class Card {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String front;
  private String back;
  private long addingTime;
  
  @ManyToOne
  @JoinColumn(name="deck_id")
  private Deck deck;

  public Deck getDeck() {
    return deck;
  }

  public void setDeck(Deck deck) {
    this.deck = deck;
  }

  public void setAddingTime(long addingTime) {
    this.addingTime = addingTime;
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

  public String getBack() {
    return back;
  }

  public void setBack(String back) {
    this.back = back;
  }

  public String getFront() {
    return front;
  }

  public void setFront(String front) {
    this.front = front;
  }
}
