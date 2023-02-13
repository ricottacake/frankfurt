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
import com.aceliq.frankfurt.models.User;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryIntegrationTest {
  
  @Autowired
  private TestEntityManager entityManager;
  
  @Test
  public void should_save_new_user() {

  }
  
}
