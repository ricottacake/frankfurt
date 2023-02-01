package com.aceliq.frankfurt.components;

import javax.sql.DataSource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.aceliq.frankfurt.models.User;
import com.aceliq.frankfurt.security.SecurityFilter;
import com.aceliq.frankfurt.models.Card;
import com.aceliq.frankfurt.models.Deck;
import com.aceliq.frankfurt.models.TimeIsOver;

@Configuration
public class AppConfig {

  @Scope(value = "prototype")
  @Bean
  public TimeIsOver runnableTaskBean(User user) {
    return new TimeIsOver(user);
  }

  @Scope(value = "prototype")
  @Bean
  public Deck deck() {
    return new Deck();
  }

  @Bean
  public User user() {
    return new User();
  }

  @Bean
  public Card card() {
    return new Card();
  }

  @Bean
  public FilterRegistrationBean<SecurityFilter> loggingFilter() {
    FilterRegistrationBean<SecurityFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(new SecurityFilter());
    registrationBean.addUrlPatterns("/api/*");
    registrationBean.setOrder(2);

    return registrationBean;
  }

  @Bean
  public DataSource userDataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(System.getenv("DATASOURCE_URL"));
    dataSource.setUsername(System.getenv("DATASOURCE_USERNAME"));
    dataSource.setPassword(System.getenv("DATASOURCE_PASSWORD"));

    return dataSource;
  }

}
