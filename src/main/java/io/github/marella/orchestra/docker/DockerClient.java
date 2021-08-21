package io.github.marella.orchestra.docker;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DockerClient {

  List<Container> list(Map<String, List<String>> filters) throws IOException;

  boolean run(Container container) throws IOException;

  boolean create(Container container) throws IOException;

  boolean start(Container container) throws IOException;

  boolean stop(Container container) throws IOException;
}
