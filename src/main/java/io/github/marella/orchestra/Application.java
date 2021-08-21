package io.github.marella.orchestra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    boolean isCommand = Console.isCommand(args);
    var app = new SpringApplication(Application.class);
    if (isCommand) {
      app.setWebApplicationType(WebApplicationType.NONE);
    }
    var context = app.run(args);
    if (isCommand) {
      System.exit(SpringApplication.exit(context));
    }
  }
}
