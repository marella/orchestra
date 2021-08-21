package io.github.marella.orchestra.master;

import io.github.marella.orchestra.store.StoreConfig;
import java.io.Closeable;
import java.io.IOException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ControllerLoop extends LeaderSelectorListenerAdapter implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(ControllerLoop.class);

  private final CuratorFramework store;
  private final StoreConfig storeConfig;
  private final ControllerConfig config;
  private final ControllerService controller;
  private final SchedulerService scheduler;
  private final LeaderSelector selector;

  public ControllerLoop(
      CuratorFramework store,
      StoreConfig storeConfig,
      ControllerConfig config,
      ControllerService controller,
      SchedulerService scheduler) {
    this.store = store;
    this.storeConfig = storeConfig;
    this.config = config;
    this.controller = controller;
    this.scheduler = scheduler;
    selector = new LeaderSelector(store, config.getLeaderElectionPath(), this);
    selector.autoRequeue();
  }

  public void start(int id) throws Exception {
    String name = String.format("controller%02d", id);
    try {
      String path = storeConfig.getIdsPath() + "/" + name;
      store.create().withMode(CreateMode.EPHEMERAL).forPath(path);
    } catch (NodeExistsException e) {
      throw new IllegalArgumentException(
          String.format("A controller with ID %d already exists", id));
    }
    controller.setId(id);
    selector.start();
  }

  @Override
  public void close() throws IOException {
    selector.close();
  }

  @Override
  public void takeLeadership(CuratorFramework client) throws Exception {
    log.info("Acquired leadership");
    try {
      runLoop();
    } catch (InterruptedException e) {
      log.warn("Interrupted");
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error("{}", e);
      throw e;
    } finally {
      log.warn("Relinquishing leadership");
    }
  }

  private void runLoop() throws Exception {
    while (true) {
      // sleep at start to handle case when we lose and acquire leadership quickly
      Thread.sleep(config.getUpdateInterval());

      log.info("Running controller");
      controller.run();

      log.info("Running scheduler");
      scheduler.run();
    }
  }
}
