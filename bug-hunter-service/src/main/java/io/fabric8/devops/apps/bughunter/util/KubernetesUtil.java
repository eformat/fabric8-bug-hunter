package io.fabric8.devops.apps.bughunter.util;

import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author kameshs
 */
public class KubernetesUtil {

    public static final String SCM_BRANCH = "SCM_BRANCH";
    public static final String SCM_REVISION = "SCM_REVISION";
    public static final String SCM_ISSUE_TRACKER_URL = "SCM_ISSUE_TRACKER_URL";
    public static final String SCM_PROJECT_URL = "SCM_PROJECT_URL";

    /**
     * 
     * @param group
     * @param project
     * @param version
     * @param provider
     * @return
     */
    public static Map<String, String> deploymentAnnotations(String group, String project,
                                                            String version, String provider) {

        Map<String, String> deploymentMedata = new HashMap<>();

        //TODO is this OK or we need to pass this based on configs
        KubernetesClient kubernetesClient = new DefaultKubernetesClient();

        Map<String, String> labelMap = new HashMap<>();
        labelMap.put("group", "io.fabric8.devops.apps");
        labelMap.put("project", "simple-calculator-springboot");
        labelMap.put("version", "1.0.0-SNAPSHOT");

        DeploymentList deploymentList = kubernetesClient
            .extensions()
            .deployments()
            .inNamespace("default")
            .withLabels(labelMap)
            .list();

        Optional<Deployment> deployment = deploymentList.getItems().stream().findFirst();

        if (deployment.isPresent()) {

            Map<String, String> annotations = deployment.get().getMetadata().getAnnotations();
            deploymentMedata.put(SCM_BRANCH, annotations.get("fabric8.io/git-branch"));
            deploymentMedata.put(SCM_REVISION, annotations.get("fabric8.io/git-commit"));
            if (annotations.containsKey("fabric8.io/project-issue-tracker-url")) {
                deploymentMedata.put(SCM_ISSUE_TRACKER_URL, annotations.get("fabric8.io/project-issue-tracker-url"));
            }
            if (annotations.containsKey("fabric8.io/project-url")) {
                deploymentMedata.put(SCM_PROJECT_URL, annotations.get("fabric8.io/project-url"));
            }
        }
        return deploymentMedata;
    }
}
