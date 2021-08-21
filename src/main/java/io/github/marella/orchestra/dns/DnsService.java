package io.github.marella.orchestra.dns;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import io.github.marella.orchestra.core.Pod;
import io.github.marella.orchestra.core.PodService;
import io.github.marella.orchestra.store.StoreConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DnsService {

  private static final Logger log = LoggerFactory.getLogger(DnsService.class);

  private final String zoneFilePath;
  private final String runningPodsPath;
  private final PodService podService;
  private final ZoneBuilder zoneBuilder;

  private volatile List<List<String>> old = null;

  public DnsService(
      DnsConfig config, StoreConfig storeConfig, PodService podService, ZoneBuilder zoneBuilder) {
    this.zoneFilePath = config.getZoneFilePath();
    this.runningPodsPath = storeConfig.getRunningPodsPath();
    this.podService = podService;
    this.zoneBuilder = zoneBuilder;
  }

  public void run() throws Exception {
    Map<String, List<Pod>> nodes = podService.getAll(runningPodsPath);
    List<List<String>> records = new ArrayList<>();
    for (List<Pod> pods : nodes.values()) {
      for (Pod pod : pods) {
        records.add(List.of(pod.getName(), pod.getIpAddress()));
      }
    }
    if (records.equals(old)) {
      log.info("New and old states are same");
      return;
    }
    String zone = zoneBuilder.build(records);
    log.info("Writing changes");
    write(zoneFilePath, zone);
    old = records;
  }

  private void write(String path, String data) throws IOException {
    Path target = Paths.get(path);
    Path temp = Paths.get(path + ".tmp");
    Files.writeString(temp, data);
    Files.move(temp, target, REPLACE_EXISTING, ATOMIC_MOVE);
  }
}
