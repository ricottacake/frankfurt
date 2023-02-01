package com.aceliq.frankfurt.exceptions;

public class DeckAlreadyExistsException extends Exception {
  public DeckAlreadyExistsException(String errorMessage) {
    super(errorMessage);
  }
}
