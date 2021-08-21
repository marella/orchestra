package io.github.marella.orchestra.worker;

import io.github.marella.orchestra.Console.ConsoleCommand;
import java.util.concurrent.Callable;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "worker", mixinStandardHelpOptions = true)
public class WorkerCommand implements Callable<Integer>, ConsoleCommand {

  private final WorkerLoop loop;

  @Option(names = "--id", required = true, description = "Unique ID for this process")
  private int id;

  public WorkerCommand(WorkerLoop loop) {
    this.loop = loop;
  }

  @Override
  public Integer call() throws Exception {
    loop.start(id);
    return 1;
  }
}
