package io.github.marella.orchestra.dns;

import io.github.marella.orchestra.Console.ConsoleCommand;
import java.util.concurrent.Callable;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "dns", mixinStandardHelpOptions = true)
public class DnsCommand implements Callable<Integer>, ConsoleCommand {

  private final DnsLoop loop;

  public DnsCommand(DnsLoop loop) {
    this.loop = loop;
  }

  @Override
  public Integer call() throws Exception {
    loop.start();
    return 1;
  }
}
