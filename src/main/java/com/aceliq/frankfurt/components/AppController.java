package com.aceliq.frankfurt.components;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.aceliq.frankfurt.dao.ApiKeyDaoImpl;
import com.aceliq.frankfurt.database.DeckDaoImpl;
import com.aceliq.frankfurt.database.UserDaoImpl;
import com.aceliq.frankfurt.exceptions.ResourceNotFoundException;
import com.aceliq.frankfurt.models.Deck;

@RestController
@Validated
public class AppController {

  @Autowired
  UserDaoImpl userDaoImpl;

  @Autowired
  ApiKeyDaoImpl apiKeyRepository;
  
  @Autowired
  DeckDaoImpl deckDaoImpl;

  @GetMapping("/api/v1/{apiKey}/decks")
  public ResponseEntity<List<Deck>> getDecks(@PathVariable("apiKey") String apiKey) {
    long telegramId = Long.parseLong(apiKey.split(":")[0]);
    String key = apiKey.split(":")[1];

    if (key.equals(apiKeyRepository.getKeyByTelegramId(telegramId)
        .orElseThrow(() -> new ResourceNotFoundException("d")))) {
      return new ResponseEntity<>(
          deckDaoImpl.getDecks(userDaoImpl.findById(telegramId).get()), HttpStatus.OK);
    } else {
      throw new ResourceNotFoundException("d");
    }
  }
}
