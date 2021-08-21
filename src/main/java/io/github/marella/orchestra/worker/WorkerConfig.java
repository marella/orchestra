package io.github.marella.orchestra.worker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("orchestra.worker")
@Data
public class WorkerConfig {
  private String network;
  private int updateInterval;
}
