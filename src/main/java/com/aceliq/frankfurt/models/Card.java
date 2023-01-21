package com.aceliq.frankfurt.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
