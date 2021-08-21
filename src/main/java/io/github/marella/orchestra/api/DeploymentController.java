package io.github.marella.orchestra.api;

import io.github.marella.orchestra.core.Deployment;
import io.github.marella.orchestra.core.DeploymentService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deployments")
public class DeploymentController {

  private final DeploymentService service;

  public DeploymentController(DeploymentService service) {
    this.service = service;
  }

  @GetMapping
  public List<Deployment> getAll() throws Exception {
    return service.getAll();
  }

  @PutMapping
  public Deployment put(@Valid @RequestBody Deployment deployment) throws Exception {
    return service.save(deployment);
  }

  @DeleteMapping("/{name}")
  public void delete(@PathVariable String name) throws Exception {
    service.delete(name);
  }
}
