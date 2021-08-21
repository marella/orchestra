package io.github.marella.orchestra.store;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchLoop implements Watcher {

  private static final Logger log = LoggerFactory.getLogger(WatchLoop.class);

  private final AtomicBoolean changed = new AtomicBoolean(false);
  private final Runnable target;
  private final int interval;
  private final int ticks;

  public WatchLoop(Runnable target, int interval, int ticks) {
    this.target = target;
    this.interval = interval;
    this.ticks = ticks;
  }

  public void process(WatchedEvent event) {
    log.info("Received event {}", event);
    changed.set(true);
  }

  public void run() throws InterruptedException {
    while (true) {
      for (int i = 0; i < ticks && !changed.compareAndSet(true, false); i++) {
        Thread.sleep(interval);
      }
      target.run();
    }
  }
}
