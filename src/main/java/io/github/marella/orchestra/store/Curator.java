package io.github.marella.orchestra.store;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @see
 *     https://github.com/apache/curator/blob/master/curator-examples/src/main/java/framework/CreateClientExamples.java
 */
@Component
public class Curator {

  private static final Logger log = LoggerFactory.getLogger(Curator.class);

  private final StoreConfig config;

  public Curator(StoreConfig config) {
    this.config = config;
  }

  @Bean
  public CuratorFramework create() throws InterruptedException, Exception {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client =
        CuratorFrameworkFactory.builder()
            .connectString(config.getConnectString())
            .sessionTimeoutMs(config.getSessionTimeout())
            .retryPolicy(retryPolicy)
            .defaultData(null)
            .build();
    client.start();
    client.blockUntilConnected();
    setup(client);
    return client;
  }

  private void setup(CuratorFramework client) throws Exception {
    ensurePath(client, config.getIdsPath());
    ensurePath(client, config.getDeploymentsPath());
    ensurePath(client, config.getDesiredPodsPath());
    ensurePath(client, config.getScheduledPodsPath());
    ensurePath(client, config.getRunningPodsPath());
  }

  private void ensurePath(CuratorFramework client, String path) throws Exception {
    try {
      client.create().creatingParentsIfNeeded().forPath(path);
      log.info("Created path {}", path);
    } catch (NodeExistsException e) {
      log.info("Path {} already exists", path);
    }
  }
}
