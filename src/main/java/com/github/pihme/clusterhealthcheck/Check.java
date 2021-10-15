package com.github.pihme.clusterhealthcheck;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import java.util.logging.Logger;

public interface Check {

  String getName();

  void run(final CoreV1Api api, String namespace, Logger logger) throws ApiException;

}
