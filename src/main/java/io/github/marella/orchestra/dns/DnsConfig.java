package io.github.marella.orchestra.dns;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("orchestra.dns")
@Data
public class DnsConfig {
  private String[] servers;
  private String[] search;
  private int updateInterval;
  private String zoneFilePath;
}
