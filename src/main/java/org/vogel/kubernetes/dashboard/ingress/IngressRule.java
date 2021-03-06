package org.vogel.kubernetes.dashboard.ingress;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1beta1HTTPIngressPath;
import io.kubernetes.client.models.V1beta1IngressRule;
import lombok.Getter;
import org.vogel.kubernetes.dashboard.KubernetesUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.vogel.kubernetes.dashboard.FormatUtils.describeBackend;

@Getter
public class IngressRule {
    private String host;
    private List<String> paths;
    private List<String> backends;

    public IngressRule(V1beta1IngressRule rule, String ns,
                       KubernetesUtils kubernetesUtils) throws ApiException {
        host = defaultIfBlank(rule.getHost(), "*");
        paths = new ArrayList<>();
        backends = new ArrayList<>();
        List<V1beta1HTTPIngressPath> ingressPaths = rule.getHttp()
                .getPaths();
        for (V1beta1HTTPIngressPath path : ingressPaths) {
            paths.add(path.getPath());
            String serviceName = path.getBackend()
                    .getServiceName();
            String servicePort;
            IntOrString backendServicePort = path.getBackend()
                    .getServicePort();
            if (backendServicePort.isInteger()) {
                servicePort = backendServicePort.getIntValue()
                        .toString();
            } else {
                servicePort = backendServicePort.getStrValue();
            }

            String describeBackend = describeBackend(ns, serviceName, servicePort, kubernetesUtils);
            backends.add(String.format("%s:%s (%s)", serviceName, servicePort, describeBackend));
        }
    }
}
