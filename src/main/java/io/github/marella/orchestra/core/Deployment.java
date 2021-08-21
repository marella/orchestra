package io.github.marella.orchestra.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Deployment {

  @NotBlank private String name;

  @NotBlank private String image;

  /** Hash to identify changes to deployment. */
  private int hash;

  @Min(1)
  private int replicas = 1;
}
