package com.aceliq.frankfurt.components;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class StartCommand extends BotCommand {

  public static final String LOGTAG = "STARTCOMMAND";

  public StartCommand() {
      super("start", "With this command you can start the Bot");
  }

  public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
    System.out.println("hello");
      /*DatabaseManager databseManager = DatabaseManager.getInstance();
      StringBuilder messageBuilder = new StringBuilder();

      String userName = user.getFirstName() + " " + user.getLastName();

      if (databseManager.getUserStateForCommandsBot(user.getId())) {
          messageBuilder.append("Hi ").append(userName).append("\n");
          messageBuilder.append("i think we know each other already!");
      } else {
          databseManager.setUserStateForCommandsBot(user.getId(), true);
          messageBuilder.append("Welcome ").append(userName).append("\n");
          messageBuilder.append("this bot will demonstrate you the command feature of the Java TelegramBots API!");
      }

      SendMessage answer = new SendMessage();
      answer.setChatId(chat.getId().toString());
      answer.setText(messageBuilder.toString());

      try {
          absSender.execute(answer);
      } catch (TelegramApiException e) {
          BotLogger.error(LOGTAG, e);
      }*/
  }
}