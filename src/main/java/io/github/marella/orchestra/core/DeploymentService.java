package io.github.marella.orchestra.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.marella.orchestra.store.StoreConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

@Service
public class DeploymentService {

  private final ObjectMapper json = new ObjectMapper();
  private final CuratorFramework store;
  private final String basePath;

  public DeploymentService(CuratorFramework store, StoreConfig storeConfig) {
    this.store = store;
    this.basePath = storeConfig.getDeploymentsPath();
  }

  public List<Deployment> getAll() throws Exception {
    List<Deployment> deployments = new ArrayList<>();
    for (String name : store.getChildren().forPath(basePath)) {
      deployments.add(get(name));
    }
    return deployments;
  }

  public Deployment get(String name) throws Exception {
    byte[] data = store.getData().forPath(getPath(name));
    return json.readValue(data, Deployment.class);
  }

  public Deployment save(Deployment deployment) throws Exception {
    deployment.setHash(Objects.hash(deployment.getImage()));
    byte[] data = json.writeValueAsBytes(deployment);
    store.create().orSetData().forPath(getPath(deployment), data);
    return deployment;
  }

  public void delete(String name) throws Exception {
    store.delete().forPath(getPath(name));
  }

  private String getPath(Deployment deployment) {
    return getPath(deployment.getName());
  }

  private String getPath(String name) {
    return basePath + "/" + name;
  }
}
