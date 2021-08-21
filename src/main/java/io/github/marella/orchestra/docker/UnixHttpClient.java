package io.github.marella.orchestra.docker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.net.URIBuilder;

public class UnixHttpClient implements AutoCloseable {

  private final CloseableHttpClient client;

  public UnixHttpClient(String path) {
    this(createHttpClient(path));
  }

  public UnixHttpClient(CloseableHttpClient client) {
    this.client = client;
  }

  public CloseableHttpResponse execute(ClassicHttpRequest request) throws IOException {
    if (request.getScheme() == null || request.getScheme().equals("unix")) {
      try {
        format(request);
      } catch (URISyntaxException e) {
        // TODO: log error
      }
    }
    return this.client.execute(request);
  }

  /** @see https://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e445 */
  public static CloseableHttpClient createHttpClient(String path) {
    var registry =
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register("unix", new UnixConnectionSocketFactory(path))
            .build();
    var manager = new PoolingHttpClientConnectionManager(registry);
    return HttpClients.custom().setConnectionManager(manager).build();
  }

  public static void format(ClassicHttpRequest request) throws URISyntaxException {
    request.setUri(format(request.getUri()));
  }

  public static URI format(URI uri) throws URISyntaxException {
    return new URIBuilder(uri).setScheme("unix").setHost("localhost").setPort(80).build();
  }

  @Override
  public void close() throws IOException {
    this.client.close();
  }
}
