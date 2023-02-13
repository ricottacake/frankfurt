package com.aceliq.frankfurt.models;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "decks", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "user_id" }) })
public class Deck {
  
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private int id;
  
  private String name;
  
  @ManyToOne
  @JoinColumn(name="user_id")
  private User owner;
  
  @OneToMany(mappedBy="deck", fetch = FetchType.EAGER)
  private List<Card> cards;
  
  public List<Card> getCards() {
    return cards;
  }
  public void setCards(List<Card> cards) {
    this.cards = cards;
  }
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public User getOwner() {
    return owner;
  }
  public void setOwner(User owner) {
    this.owner = owner;
  }
}
