package io.github.marella.orchestra.master;

import io.github.marella.orchestra.core.Deployment;
import io.github.marella.orchestra.core.DeploymentService;
import io.github.marella.orchestra.core.IdGenerator;
import io.github.marella.orchestra.core.Pod;
import io.github.marella.orchestra.core.PodService;
import io.github.marella.orchestra.store.StoreConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ControllerService {

  private static final Logger log = LoggerFactory.getLogger(ControllerService.class);

  private final CuratorFramework store;
  private final DeploymentService deploymentService;
  private final PodService podService;

  private final String podsPath;

  private IdGenerator ids;

  public ControllerService(
      CuratorFramework store,
      StoreConfig storeConfig,
      DeploymentService deploymentService,
      PodService podService) {
    this.store = store;
    this.deploymentService = deploymentService;
    this.podService = podService;
    this.podsPath = storeConfig.getDesiredPodsPath();
  }

  public void setId(int id) {
    if (ids != null) {
      throw new IllegalStateException("IdGenerator is already set");
    }
    ids = new IdGenerator(id);
  }

  public void run() throws InterruptedException, Exception {
    if (ids == null) {
      throw new IllegalStateException("IdGenerator is not set");
    }

    List<Deployment> deployments = deploymentService.getAll();
    List<Pod> current = podService.get(podsPath);
    List<Pod> desired = createPods(current, deployments);
    if (desired.equals(current)) {
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
    podService.set(podsPath, desired);
  }

  private List<Pod> createPods(List<Pod> current, List<Deployment> deployments) {
    Map<String, Deployment> deps = new HashMap<>();
    Map<String, Integer> replicas = new HashMap<>();
    for (Deployment deployment : deployments) {
      String name = deployment.getName();
      deps.put(name, deployment);
      replicas.put(name, deployment.getReplicas());
    }
    List<Pod> desired = new ArrayList<>();

    // Remove extra/outdated pods from existing
    for (Pod pod : current) {
      String name = pod.getName();
      if (!replicas.containsKey(name) || deps.get(name).getHash() != pod.getHash()) {
        continue;
      }
      int count = replicas.get(name) - 1;
      if (count == 0) {
        replicas.remove(name);
      } else {
        replicas.put(name, count);
      }
      desired.add(pod);
    }

    // Create and add new pods
    for (var entry : replicas.entrySet()) {
      String name = entry.getKey();
      for (int i = 0; i < entry.getValue(); i++) {
        Pod pod = createPod(deps.get(name));
        desired.add(pod);
      }
    }

    return desired;
  }

  private Pod createPod(Deployment deployment) {
    Pod pod = new Pod();
    pod.setName(deployment.getName());
    pod.setImage(deployment.getImage());
    pod.setHash(deployment.getHash());
    pod.setUid(ids.next());
    return pod;
  }
}
