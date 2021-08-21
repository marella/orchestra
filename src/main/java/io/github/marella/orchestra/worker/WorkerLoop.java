package io.github.marella.orchestra.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkerLoop {

  private static final Logger log = LoggerFactory.getLogger(WorkerLoop.class);

  private final WorkerConfig config;
  private final WorkerService worker;

  public WorkerLoop(WorkerConfig config, WorkerService worker) {
    this.config = config;
    this.worker = worker;
  }

  public void start(int id) throws Exception {
    String name = String.format("node%02d", id);
    worker.init(name);
    runLoop();
  }

  private void runLoop() throws Exception {
    while (true) {
      Thread.sleep(config.getUpdateInterval());

      log.info("Running worker");
      worker.run();
    }
  }
}
