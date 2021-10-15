package com.github.pihme.clusterhealthcheck.checks;

import com.github.pihme.clusterhealthcheck.Check;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks ready status of pods in a namespace. If any are not ready, this is logged as a warning. If
 * all are ready, this is logged as info
 */
public class PodsAreReady implements Check {

  @Override
  public String getName() {
    return "Pods are Ready";
  }

  @Override
  public void run(final CoreV1Api api, final String namespace, final Logger logger)
      throws ApiException {

    boolean foundDeviations = false;

    final V1PodList list = api.listNamespacedPod(namespace, null, null, null, null, null, null,
        null, null, null, null);
    for (final V1Pod item : list.getItems()) {
      final var ready = item.getStatus().getContainerStatuses().get(0)
          .getReady();

      if (!ready) {
        foundDeviations = true;
        logger.warning("Pod " + item.getMetadata().getName() + " is not ready");
      }
    }

    if (!foundDeviations) {
      logger.log(Level.INFO, "All pods are ready +1");
    }
  }


}
