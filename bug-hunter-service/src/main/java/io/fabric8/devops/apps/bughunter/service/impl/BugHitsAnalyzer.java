package io.fabric8.devops.apps.bughunter.service.impl;

import io.fabric8.devops.apps.bughunter.model.AppInfo;
import io.fabric8.devops.apps.bughunter.model.BugInfo;
import io.fabric8.devops.apps.bughunter.model.PodInfo;
import io.fabric8.devops.apps.bughunter.util.KubernetesUtil;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * @author kameshs
 */
public class BugHitsAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BugHitsAnalyzer.class);


    public static Optional<BugInfo> process(JsonObject hit) {

        LOGGER.trace("Starting Bug Hits Analyzer");

        Optional<BugInfo> optionalBugInfo = Optional.empty();

        BugInfo bugInfo = new BugInfo();
        AppInfo appInfo = new AppInfo();
        PodInfo podInfo = new PodInfo();

        LOGGER.trace("Hit:{}", hit);

        if (hit != null) {

            String id = hit.getString("_id");
            float score = hit.getFloat("_score");

            JsonObject msgSource = hit.getJsonObject("_source");

            JsonObject kubernetes = msgSource.getJsonObject("kubernetes");
            String k8sNamespace = kubernetes.getString("namespace_name");
            String k8sPodId = kubernetes.getString("pod_id");
            String k8sPodName = kubernetes.getString("pod_name");
            String podHostName = kubernetes.getString("host");
            String containerName = kubernetes.getString("container_name");

            JsonObject labels = kubernetes.getJsonObject("labels");
            String group = labels.containsKey("group") ? labels.getString("group") : "";
            String project = labels.containsKey("project") ? labels.getString("project") : "";
            String version = labels.containsKey("version") ? labels.getString("version") : "";
            String provider = labels.containsKey("version") ? labels.getString("provider") : "";

            Map<String, String> appMetadata = KubernetesUtil.deploymentAnnotations(group, project, version, provider);

            //Application specific information

            LOGGER.trace("App Metadata:{}", appMetadata);

            appInfo
                .setBranch(appMetadata.containsKey(KubernetesUtil.SCM_BRANCH)
                    ? appMetadata.get(KubernetesUtil.SCM_BRANCH) : "")
                .setGroup(group)
                .setIssueTrackerUrl(appMetadata.containsKey(KubernetesUtil.SCM_ISSUE_TRACKER_URL)
                    ? appMetadata.get(KubernetesUtil.SCM_ISSUE_TRACKER_URL) : "")
                .setProject(project)
                .setProjectUrl(appMetadata.containsKey(KubernetesUtil.SCM_PROJECT_URL)
                    ? appMetadata.get(KubernetesUtil.SCM_PROJECT_URL) : "")
                .setRevision(appMetadata.containsKey(KubernetesUtil.SCM_REVISION)
                    ? appMetadata.get(KubernetesUtil.SCM_REVISION) : "")
                .setVersion(version);

            bugInfo.setApp(appInfo);

            //Add Pod Related Information
            podInfo
                .setContainerName(containerName)
                .setHost(podHostName)
                .setNamespace(k8sNamespace)
                .setPodId(k8sPodId)
                .setPodName(k8sPodName);

            bugInfo.setPod(podInfo);

            //Retrieve Log Info
            String logMessage = msgSource.getString("log");
            String timestamp = msgSource.getString("@timestamp");

            return Optional.of(bugInfo.setId(id)
                .setScore(score)
                .setTimestamp(timestamp)
                .setLogMessage(logMessage));

        }

        return optionalBugInfo;
    }
}
