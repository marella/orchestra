package io.github.marella.orchestra.worker;

import io.github.marella.orchestra.core.Pod;
import io.github.marella.orchestra.core.PodService;
import io.github.marella.orchestra.dns.DnsConfig;
import io.github.marella.orchestra.docker.Container;
import io.github.marella.orchestra.docker.DockerClient;
import io.github.marella.orchestra.store.StoreConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkerService {

  private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

  private final CuratorFramework store;
  private final StoreConfig storeConfig;
  private final PodService podService;
  private final DockerClient docker;
  private final WorkerConfig config;
  private final DnsConfig dnsConfig;

  private boolean initialized = false;
  private String name;
  private String scheduledPodsPath;
  private String runningPodsPath;

  public WorkerService(
      CuratorFramework store,
      StoreConfig storeConfig,
      PodService podService,
      DockerClient docker,
      WorkerConfig config,
      DnsConfig dnsConfig) {
    this.store = store;
    this.storeConfig = storeConfig;
    this.podService = podService;
    this.docker = docker;
    this.config = config;
    this.dnsConfig = dnsConfig;
  }

  public void init(String name) throws Exception {
    if (initialized) {
      throw new IllegalStateException("WorkerService is already initialized");
    }
    this.name = name;
    scheduledPodsPath = storeConfig.getScheduledPodsPath() + "/" + name;
    runningPodsPath = storeConfig.getRunningPodsPath() + "/" + name;
    try {
      String path = storeConfig.getIdsPath() + "/" + name;
      store.create().withMode(CreateMode.EPHEMERAL).forPath(path);
    } catch (NodeExistsException e) {
      throw new IllegalArgumentException(
          String.format("A worker with name %s already exists", name));
    }
    store.create().withMode(CreateMode.EPHEMERAL).forPath(scheduledPodsPath);
    store.create().withMode(CreateMode.EPHEMERAL).forPath(runningPodsPath);
    initialized = true;
  }

  public void run() throws InterruptedException, Exception {
    if (!initialized) {
      throw new IllegalStateException("WorkerService is not initialized");
    }
    List<Pod> podsList = podService.get(scheduledPodsPath);
    Map<Long, Pod> pods = new HashMap<>();
    for (Pod pod : podsList) {
      pods.put(pod.getUid(), pod);
    }
    List<Container> current = getRunningContainers();

    log.info("Checking and removing outdated containers");
    for (Container container : current) {
      Pod pod = toPod(container);
      long uid = pod.getUid();
      if (pods.containsKey(uid)) {
        pods.remove(uid);
      } else {
        docker.stop(container);
      }
    }

    if (!pods.isEmpty()) {
      log.info("Creating and starting new containers");
      for (Pod pod : pods.values()) {
        Container container = toContainer(pod);
        docker.run(container);
      }
    }

    updateRunningPods(); // TODO: create separate DiscoveryService
  }

  private void updateRunningPods() throws Exception {
    log.info("Updating running pods info");
    List<Pod> podsList = podService.get(scheduledPodsPath);
    Map<Long, Pod> pods = new LinkedHashMap<>();
    for (Pod pod : podsList) {
      pods.put(pod.getUid(), pod);
    }
    List<Container> containers = getRunningContainers();
    List<Pod> running = new ArrayList<>();
    for (Container container : containers) {
      Pod cPod = toPod(container);
      long uid = cPod.getUid();
      if (!pods.containsKey(uid)) {
        continue;
      }
      Pod pod = pods.get(uid);
      pod.setIpAddress(cPod.getIpAddress());
      running.add(pod);
    }
    List<Pod> old = podService.get(runningPodsPath);
    if (running.equals(old)) {
      log.info("New and old state are same");
      return;
    }
    log.info("Writing changes");
    podService.set(runningPodsPath, running);
  }

  private List<Container> getRunningContainers() throws IOException {
    Map<String, List<String>> filters = new HashMap<>();
    filters.put("status", List.of("running"));
    filters.put("label", List.of(getNodeLabelKey() + "=" + getNodeLabelValue()));
    return docker.list(filters);
  }

  private Container toContainer(Pod pod) {
    Container container = new Container();
    container.setImage(pod.getImage());
    container.setLabels(new HashMap<>());
    container.getLabels().put(getNodeLabelKey(), getNodeLabelValue());
    container.getLabels().put(getPodLabelKey(), String.valueOf(pod.getUid()));
    container.setHostConfig(new Container.HostConfig());
    container.getHostConfig().setAutoRemove(true);
    container.getHostConfig().setNetworkMode(config.getNetwork());
    container.getHostConfig().setDns(dnsConfig.getServers());
    container.getHostConfig().setDnsSearch(dnsConfig.getSearch());
    return container;
  }

  private Pod toPod(Container container) {
    Pod pod = new Pod();
    long uid = Long.parseLong(container.getLabels().get(getPodLabelKey()));
    pod.setUid(uid);
    pod.setImage(container.getImage());
    pod.setIpAddress(container.getIpAddress());
    return pod;
  }

  private String getPodLabelKey() {
    return Pod.class.getName();
  }

  private String getNodeLabelKey() {
    return WorkerService.class.getName();
  }

  private String getNodeLabelValue() {
    return name;
  }
}
