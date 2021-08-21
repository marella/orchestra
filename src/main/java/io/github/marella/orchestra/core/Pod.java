package io.github.marella.orchestra.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Pod {

  public static final TypeReference<List<Pod>> LIST_TYPE = new TypeReference<>() {};

  /** Unique identifier for each pod. */
  private long uid;

  /** Name of deployment. */
  private String name;

  private String image;

  /** Hash to identify changes to deployment. */
  private int hash;

  private String ipAddress;
}
