package io.github.marella.orchestra.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Component;

@Component
public class DockerClientImpl extends UnixHttpClient implements DockerClient {

  private static final ObjectMapper mapper = new ObjectMapper();

  public DockerClientImpl() {
    this("/var/run/docker.sock");
  }

  public DockerClientImpl(String path) {
    super(path);
  }

  public List<Container> list(Map<String, List<String>> filters) throws IOException {
    URI uri;
    try {
      uri =
          new URIBuilder("/containers/json")
              .setParameter("filters", mapper.writeValueAsString(filters))
              .build();
    } catch (URISyntaxException e) {
      // TODO: use proper error
      throw new RuntimeException(e);
    }
    var request = new HttpGet(uri);
    List<Container> containers;
    try (var response = execute(request)) {
      containers = mapper.readValue(response.getEntity().getContent(), Container.LIST_TYPE);
    }
    return containers;
  }

  public boolean run(Container container) throws IOException {
    create(container);
    start(container);
    return true;
  }

  public boolean create(Container container) throws IOException {
    var request = new HttpPost("/containers/create");
    var data = mapper.writeValueAsString(container);
    request.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
    try (var response = execute(request)) {
      var res = mapper.readValue(response.getEntity().getContent(), Container.class);
      container.setId(res.getId());
    }
    return true;
  }

  public boolean start(Container container) throws IOException {
    var request = new HttpPost("/containers/" + container.getId() + "/start");
    execute(request).close();
    return true;
  }

  public boolean stop(Container container) throws IOException {
    var request = new HttpPost("/containers/" + container.getId() + "/stop");
    execute(request).close();
    return true;
  }
}
