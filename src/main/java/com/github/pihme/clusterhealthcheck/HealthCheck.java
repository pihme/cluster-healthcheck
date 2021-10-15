package com.github.pihme.clusterhealthcheck;

import com.github.pihme.clusterhealthcheck.checks.PodsAreReady;
import com.github.pihme.clusterhealthcheck.k8s.ClientFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs health checks for a given cluster.<br>
 *
 * Takes two arguments:
 * <ol>
 *   <li>Name of Kubernetes context</li>
 *   <li>Name of namespace</li>
 * </ol>
 *
 * <strong>Notes</strong><br>
 * <ul>
 *   <li>This application assumes that there is <code>/.kube/config</code> in the user's home
 *   <li>Most likely you need to have kubectl installed</li>
 *   <li>If authentication tokens are no longer valid, you need to authenticate through other means
 *   <li>Currently, there is no significant error handling
 *   <li>Messages are written to the log; use log level to filter out verbose information
 * </ul>
 */
public class HealthCheck {

  private static final List<? extends Check> CHECKS = List.of(new PodsAreReady());
  private static final Logger LOGGER = Logger.getLogger("HealthCheck");

  public static void main(final String[] args) {
    try {

      final var k8sContext = args[0];
      final var cluster = args[1];

      LOGGER.info("Checking cluster " + cluster + " in " + k8sContext);

      LOGGER.info("Creating API Client");
      // loading the out-of-cluster config, a kubeconfig from file-system
      final ApiClient client = new ClientFactory().buildClient(k8sContext);

      // set the global default api-client to the in-cluster one from above
      Configuration.setDefaultApiClient(client);

      LOGGER.info("Creating API");
      final CoreV1Api api = new CoreV1Api();

      LOGGER.info("Running checks");
      CHECKS.forEach(check -> runCheck(check, api, cluster, LOGGER));
      LOGGER.info("Finished running checks");
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  private static void runCheck(final Check check, final CoreV1Api api, final String namespace,
      final Logger logger) {
    LOGGER.info("Running: " + check.getName());
    try {
      check.run(api, namespace, logger);
    } catch (final ApiException e) {
      logger.log(Level.SEVERE, "Exception during check " + e.getMessage(), e);
    }
  }
}