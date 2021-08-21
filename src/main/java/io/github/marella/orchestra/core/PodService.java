package io.github.marella.orchestra.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PodService {

  private static final Logger log = LoggerFactory.getLogger(PodService.class);

  private final ObjectMapper json = new ObjectMapper();
  private final CuratorFramework store;

  public PodService(CuratorFramework store) {
    this.store = store;
  }

  public Map<String, List<Pod>> getAll(String path) throws Exception {
    Map<String, List<Pod>> pods = new LinkedHashMap<>();
    for (String name : store.getChildren().forPath(path)) {
      pods.put(name, get(path + "/" + name));
    }
    return pods;
  }

  public List<Pod> get(String path) throws Exception {
    byte[] data = store.getData().forPath(path);
    if (data == null || data.length == 0) {
      return List.of();
    }
    return json.readValue(data, Pod.LIST_TYPE);
  }

  public void setAll(String path, Map<String, List<Pod>> pods) throws Exception {
    for (var entry : pods.entrySet()) {
      String name = entry.getKey();
      set(path + "/" + name, entry.getValue());
    }
  }

  public void set(String path, List<Pod> pods) throws Exception {
    byte[] data = json.writeValueAsBytes(pods);
    store.setData().forPath(path, data);
  }
}
