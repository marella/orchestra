package io.github.marella.orchestra.docker;

import io.github.marella.orchestra.core.Pod;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDockerClient implements DockerClient {

  private Map<Long, Container> containers = new HashMap<>();

  public List<Container> list(Map<String, List<String>> filters) throws IOException {
    return List.copyOf(containers.values());
  }

  public boolean run(Container container) throws IOException {
    create(container);
    start(container);
    return true;
  }

  public boolean create(Container container) throws IOException {
    containers.put(getKey(container), container);
    return true;
  }

  public boolean start(Container container) throws IOException {
    container.setNetworkSettings(new Container.NetworkSettings());
    container.getNetworkSettings().setNetworks(new HashMap<>());
    var network = new Container.NetworkSettings.Network();
    network.setIpAddress("ip");
    container.getNetworkSettings().getNetworks().put("test", network);
    return true;
  }

  public boolean stop(Container container) throws IOException {
    containers.remove(getKey(container));
    return true;
  }

  private long getKey(Container container) {
    return Long.parseLong(container.getLabels().get(Pod.class.getName()));
  }
}
