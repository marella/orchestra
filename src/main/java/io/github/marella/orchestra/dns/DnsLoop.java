package io.github.marella.orchestra.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DnsLoop {

  private static final Logger log = LoggerFactory.getLogger(DnsLoop.class);

  private final DnsConfig config;
  private final DnsService service;

  public DnsLoop(DnsConfig config, DnsService service) {
    this.config = config;
    this.service = service;
  }

  public void start() throws Exception {
    runLoop();
  }

  private void runLoop() throws Exception {
    while (true) {
      Thread.sleep(config.getUpdateInterval());

      log.info("Running DNS service");
      service.run();
    }
  }
}
