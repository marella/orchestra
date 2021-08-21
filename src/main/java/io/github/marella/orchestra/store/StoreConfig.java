package io.github.marella.orchestra.store;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("orchestra.store")
@Data
public class StoreConfig {
  private String connectString;
  private int sessionTimeout;
  private String idsPath;
  private String deploymentsPath;
  private String desiredPodsPath;
  private String scheduledPodsPath;
  private String runningPodsPath;
}
