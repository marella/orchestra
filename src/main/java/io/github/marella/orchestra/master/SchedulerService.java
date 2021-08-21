package io.github.marella.orchestra.master;

import io.github.marella.orchestra.core.Pod;
import io.github.marella.orchestra.core.PodService;
import io.github.marella.orchestra.store.StoreConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

  private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

  private final CuratorFramework store;
  private final PodService podService;

  private final String desiredPodsPath;
  private final String scheduledPodsPath;

  public SchedulerService(CuratorFramework store, StoreConfig storeConfig, PodService podService) {
    this.store = store;
    this.podService = podService;
    this.desiredPodsPath = storeConfig.getDesiredPodsPath();
    this.scheduledPodsPath = storeConfig.getScheduledPodsPath();
  }

  public void run() throws InterruptedException, Exception {
    List<Pod> pods = podService.get(desiredPodsPath);
    Map<String, List<Pod>> current = podService.getAll(scheduledPodsPath);
    Map<String, List<Pod>> desired = assignPods(current, pods);
    if (desired.equals(current)) {
      // TODO: compare node by node and only update changed nodes
      log.info("Desired and current states are same");
      return;
    }

    if (Thread.interrupted()) { // clears interrupted status
      // check if the current instance is still the leader before writing changes
      // LeaderSelector instance will interrupt this thread when connection is lost
      // as the current instance might no longer be the leader
      // @see https://curator.apache.org/curator-recipes/leader-election.html
      throw new InterruptedException();
    }

    log.info("Writing changes");
    podService.setAll(scheduledPodsPath, desired);
  }

  private Map<String, List<Pod>> assignPods(Map<String, List<Pod>> current, List<Pod> podsList) {
    Map<Long, Pod> pods = new LinkedHashMap<>();
    for (Pod pod : podsList) {
      pods.put(pod.getUid(), pod);
    }
    Map<String, List<Pod>> desired = new LinkedHashMap<>();

    // Remove extra pods from existing
    for (var entry : current.entrySet()) {
      String node = entry.getKey();
      desired.put(node, new ArrayList<>());
      for (Pod pod : entry.getValue()) {
        long uid = pod.getUid();
        if (!pods.containsKey(uid)) {
          continue;
        }
        desired.get(node).add(pods.remove(uid));
      }
    }

    // Schedule new/unassigned pods
    List<Pod> unassigned = new ArrayList<>(pods.values());
    assignNewPods(desired, unassigned);

    return desired;
  }

  private void assignNewPods(Map<String, List<Pod>> nodes, List<Pod> unassigned) {
    if (nodes.size() == 0) {
      return;
    }
    outer:
    while (!unassigned.isEmpty()) {
      for (List<Pod> pods : nodes.values()) {
        if (unassigned.isEmpty()) {
          break outer;
        }
        // Add pods in reverse order and add extra pods to starting nodes.
        // This assigns more pods to starting nodes but Controller removes
        // last pods first so starting nodes will have more pods removed
        // which hopefully balances pod distribution across nodes.
        pods.add(unassigned.remove(unassigned.size() - 1));
      }
    }
  }
}
