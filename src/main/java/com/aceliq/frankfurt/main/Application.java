package com.aceliq.frankfurt.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan({"com.aceliq.frankfurt.database", "com.aceliq.frankfurt.components"})
@EnableJpaRepositories({"com.aceliq.frankfurt.database"})
@EnableScheduling
@EntityScan("com.aceliq.frankfurt.models")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
