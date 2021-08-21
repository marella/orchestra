package io.github.marella.orchestra.master;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("orchestra.controller")
@Data
public class ControllerConfig {
  private String leaderElectionPath;
  private int updateInterval;
}
