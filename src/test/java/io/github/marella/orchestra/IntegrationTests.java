package io.github.marella.orchestra;

import static org.junit.jupiter.api.Assertions.*;

import io.github.marella.orchestra.core.Deployment;
import io.github.marella.orchestra.core.DeploymentService;
import io.github.marella.orchestra.core.PodService;
import io.github.marella.orchestra.docker.DockerClient;
import io.github.marella.orchestra.docker.TestDockerClient;
import io.github.marella.orchestra.master.ControllerService;
import io.github.marella.orchestra.master.SchedulerService;
import io.github.marella.orchestra.store.StoreConfig;
import io.github.marella.orchestra.worker.WorkerService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class IntegrationTests {

  static TestingServer zkServer;
  static WorkerService worker;

  @TestConfiguration
  static class IntegrationTestsConfiguration {

    @Bean
    public DockerClient docker() {
      return new TestDockerClient();
    }
  }

  @BeforeAll
  static void startZooKeeper() throws Exception {
    zkServer = new TestingServer(2181, true);
  }

  @AfterAll
  static void stopZooKeeper() throws Exception {
    zkServer.close();
  }

  @Autowired CuratorFramework store;
  @Autowired StoreConfig storeConfig;
  @Autowired DeploymentService deploymentService;
  @Autowired PodService podService;
  @Autowired ControllerService controller;
  @Autowired SchedulerService scheduler;
  @Autowired WorkerService workerService;

  @Test
  @Order(1)
  void testStore() {
    assertNotNull(store);
  }

  @Test
  @Order(2)
  void testApi() throws Exception {
    assertEquals(0, deploymentService.getAll().size());
    var deployment = new Deployment();
    deployment.setName("foo");
    deployment.setImage("bar");
    deploymentService.save(deployment);
    var deployments = deploymentService.getAll();
    assertEquals(1, deployments.size());
    assertEquals("foo", deployments.get(0).getName());
    assertEquals("bar", deployments.get(0).getImage());

    deployment = new Deployment();
    deployment.setName("foo2");
    deployment.setImage("bar2");
    deploymentService.save(deployment);
    deployments = deploymentService.getAll();
    assertEquals(2, deployments.size());
    assertTrue(
        deployments.get(0).getName().equals("foo2") || deployments.get(1).getName().equals("foo2"));
    assertTrue(
        deployments.get(0).getImage().equals("bar2")
            || deployments.get(1).getImage().equals("bar2"));

    deploymentService.delete("foo2");
    deployments = deploymentService.getAll();
    assertEquals(1, deployments.size());
    assertEquals("foo", deployments.get(0).getName());
    assertEquals("bar", deployments.get(0).getImage());
  }

  @Test
  @Order(3)
  void testController() throws Exception {
    var path = storeConfig.getDesiredPodsPath();
    var pods = podService.get(path);
    assertEquals(0, pods.size());
    controller.setId(1);
    controller.run();
    pods = podService.get(path);
    assertEquals(1, pods.size());
    assertEquals("foo", pods.get(0).getName());
    assertEquals("bar", pods.get(0).getImage());
  }

  @Test
  @Order(4)
  void testScheduler() throws Exception {
    assertNull(worker);
    worker = workerService;
    worker.init("node01");
    var path = storeConfig.getScheduledPodsPath() + "/node01";
    var pods = podService.get(path);
    assertEquals(0, pods.size());
    scheduler.run();
    pods = podService.get(path);
    assertEquals(1, pods.size());
    assertEquals("foo", pods.get(0).getName());
    assertEquals("bar", pods.get(0).getImage());
  }

  @Test
  @Order(5)
  void testWorker() throws Exception {
    assertNotNull(worker);
    var path = storeConfig.getRunningPodsPath() + "/node01";
    var pods = podService.get(path);
    assertEquals(0, pods.size());
    worker.run();
    pods = podService.get(path);
    assertEquals(1, pods.size());
    assertEquals("foo", pods.get(0).getName());
    assertEquals("bar", pods.get(0).getImage());
  }
}
