package com.aceliq.frankfurt.main.test;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import com.aceliq.frankfurt.database.CardRepository;
import com.aceliq.frankfurt.database.DeckRepository;
import com.aceliq.frankfurt.database.UserRepository;
import com.aceliq.frankfurt.models.User;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryIntegrationTest {
  
  @Autowired
  private TestEntityManager entityManager;
  
  @Autowired private UserRepository userRepository;
  @Autowired private CardRepository cardRepository;
  @Autowired private DeckRepository deckRepository;
  
  @Test
  public void should_save_new_user() {
    User user = new User();
    user.setJoinDate(111);
    user.setTelegramId(555);
    user.setLanguage("en");
    
    User savedUser = userRepository.save(user);
    
    assertThat(savedUser).hasFieldOrPropertyWithValue("joinDate", 111L);
    assertThat(savedUser).hasFieldOrPropertyWithValue("telegramId", 555L);
    assertThat(savedUser).hasFieldOrPropertyWithValue("language", "en");
  }
  
}
