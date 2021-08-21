package io.github.marella.orchestra.docker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

/**
 * @see
 *     https://github.com/apache/httpcomponents-client/blob/master/httpclient5/src/main/java/org/apache/hc/client5/http/socket/PlainConnectionSocketFactory.java
 * @see
 *     https://github.com/spotify/docker-client/blob/master/src/main/java/com/spotify/docker/client/UnixConnectionSocketFactory.java
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class UnixConnectionSocketFactory implements ConnectionSocketFactory {

  private final String path;

  public UnixConnectionSocketFactory(String path) {
    this.path = path;
  }

  @Override
  public Socket createSocket(final HttpContext context) throws IOException {
    return UnixSocketChannel.create().socket();
  }

  @Override
  public Socket connectSocket(
      final TimeValue connectTimeout,
      final Socket socket,
      final HttpHost host,
      final InetSocketAddress remoteAddress,
      final InetSocketAddress localAddress,
      final HttpContext context)
      throws IOException {
    socket.setSoTimeout(connectTimeout.toMillisecondsIntBound());
    socket.connect(new UnixSocketAddress(path));
    return socket;
  }
}
