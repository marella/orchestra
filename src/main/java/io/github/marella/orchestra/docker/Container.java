package io.github.marella.orchestra.docker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Container {

  public static final TypeReference<List<Container>> LIST_TYPE = new TypeReference<>() {};

  @JsonProperty("Id")
  private String id;

  @JsonProperty("Names")
  private String[] names;

  @JsonProperty("Image")
  private String image;

  @JsonProperty("Labels")
  private Map<String, String> labels;

  @JsonProperty("HostConfig")
  private HostConfig hostConfig;

  @JsonProperty("NetworkSettings")
  private NetworkSettings networkSettings;

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  public static class HostConfig {

    @JsonProperty("AutoRemove")
    private Boolean autoRemove;

    @JsonProperty("NetworkMode")
    private String networkMode;

    @JsonProperty("Dns")
    private String[] dns;

    @JsonProperty("DnsSearch")
    private String[] dnsSearch;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  public static class NetworkSettings {

    @JsonProperty("Networks")
    private Map<String, Network> networks;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Network {

      @JsonProperty("IPAddress")
      private String ipAddress;
    }
  }

  @JsonIgnore
  public String getIpAddress() {
    return getNetworkSettings().getNetworks().values().iterator().next().getIpAddress();
  }
}
