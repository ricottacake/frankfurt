package com.aceliq.frankfurt.components;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.aceliq.frankfurt.dao.ApiKey;
import com.aceliq.frankfurt.database.DeckRepository;
import com.aceliq.frankfurt.database.UserRepository;
import com.aceliq.frankfurt.models.Deck;

@RestController
@Validated
public class AppController {

  @Autowired
  DeckRepository deckRepository;
  
  @Autowired
  UserRepository userRepository;
  
  @Autowired
  ApiKey apiKeyRepository;
  
  @GetMapping("/api/v1/{apiKey}/decks")
  public List<Deck> getDecks(@PathVariable("apiKey") String apiKey) {
    long telegramId = Long.parseLong(apiKey.split(":")[0]);
    String key = apiKey.split(":")[1];
    
    if(key.equals(apiKeyRepository.getKeyByTelegramId(telegramId))) {
      return deckRepository.findByOwner(userRepository.findById(telegramId).get());
    }
    return null;
  }
}
