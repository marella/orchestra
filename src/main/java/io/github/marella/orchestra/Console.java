package io.github.marella.orchestra;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;

@Component
public class Console implements CommandLineRunner, ExitCodeGenerator {

  private static final String PREFIX = "run";

  private final IFactory factory;

  private final List<ConsoleCommand> commands;

  private int exitCode;

  public interface ConsoleCommand {}

  @Command(name = PREFIX)
  private static class MainCommand {}

  public Console(IFactory factory, List<ConsoleCommand> commands) {
    this.factory = factory;
    this.commands = List.copyOf(commands);
  }

  @Override
  public void run(String... args) {
    if (!isCommand(args)) {
      return;
    }
    args = Arrays.copyOfRange(args, 1, args.length);
    var cli = new CommandLine(new MainCommand(), factory);
    for (var command : commands) {
      cli.addSubcommand(command);
    }
    exitCode = cli.execute(args);
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }

  static boolean isCommand(String... args) {
    return args.length > 0 && args[0].equals(PREFIX);
  }
}
