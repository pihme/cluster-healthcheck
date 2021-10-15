package com.github.pihme.clusterhealthcheck.k8s;

import com.google.auth.oauth2.GoogleCredentials;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import java.io.FileReader;
import java.io.IOException;

public class ClientFactory {

  public ApiClient buildClient(final String k8sContext) throws IOException {
    // file path to your KubeConfig
    final String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

    KubeConfig.registerAuthenticator(
        new ReplacedGCPAuthenticator(GoogleCredentials.getApplicationDefault()));

    final var config = KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath));
    config.setContext(k8sContext);

    // loading the out-of-cluster config, a kubeconfig from file-system
    return ClientBuilder.kubeconfig(config)
        .build();
  }
}
