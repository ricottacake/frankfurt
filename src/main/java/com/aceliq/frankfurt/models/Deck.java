package com.aceliq.frankfurt.models;

import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "decks")
public class Deck {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  
  private String name;
  
  @ManyToOne
  @JoinColumn(name="user_id")
  private User owner;
  
  @OneToMany(mappedBy="id")
  private List<Card> cards;
  
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
