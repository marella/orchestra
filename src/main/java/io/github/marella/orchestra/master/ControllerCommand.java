package io.github.marella.orchestra.master;

import io.github.marella.orchestra.Console.ConsoleCommand;
import java.util.concurrent.Callable;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "controller", mixinStandardHelpOptions = true)
public class ControllerCommand implements Callable<Integer>, ConsoleCommand {

  private final ControllerLoop loop;

  @Option(names = "--id", required = true, description = "Unique ID for this process")
  private int id;

  public ControllerCommand(ControllerLoop loop) {
    this.loop = loop;
  }

  @Override
  public Integer call() throws Exception {
    loop.start(id);
    Thread.sleep(Long.MAX_VALUE);
    return 1;
  }
}
