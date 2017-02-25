package io.fabric8.devops.apps.bughunter;

import io.fabric8.devops.apps.bughunter.service.LogsAnalyzerService;
import io.fabric8.devops.apps.bughunter.service.impl.ExceptionsLogsAnalyzer;
import io.fabric8.devops.apps.elasticsearch.helper.service.ElasticSearchService;
import io.fabric8.devops.apps.elasticsearch.helper.service.impl.ElasticSearchServiceImpl;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kameshs
 */
public class BugHunterMainVerticle extends AbstractVerticle {


    private static final Logger LOGGER = LoggerFactory.getLogger(BugHunterMainVerticle.class);

    @Override
    public void start() throws Exception {

        //Register LogAnalyzer Services ...
        LogsAnalyzerService exceptionAnalyzerService = new ExceptionsLogsAnalyzer(vertx);
        ProxyHelper.registerService(LogsAnalyzerService.class, vertx, exceptionAnalyzerService,
            LogsAnalyzerService.EXCEPTIONS_EVENT_BUS_ADDR);

        LOGGER.debug("Exception Log Analyzer Service available at address {}", LogsAnalyzerService.EXCEPTIONS_EVENT_BUS_ADDR);

        final ConfigStoreOptions cfgStoreOpts = new ConfigStoreOptions()
            .setType("env");

        final ConfigRetrieverOptions cfgRetrieverOptions = new ConfigRetrieverOptions();
        cfgRetrieverOptions
            .addStore(cfgStoreOpts);

        ConfigRetriever configMapRetriever = ConfigRetriever.create(vertx, cfgRetrieverOptions);

        configMapRetriever.getConfig(bugHunterConfig -> {
            if (bugHunterConfig.succeeded()) {

                JsonObject configData = bugHunterConfig.result();

                LOGGER.trace("Deploying Bug Hunter with ES options {}", configData);

                if (!configData.isEmpty()) {

                    ElasticSearchService elasticSearchService = new ElasticSearchServiceImpl(vertx, configData);
                    ProxyHelper.registerService(ElasticSearchService.class, vertx, elasticSearchService,
                        ElasticSearchService.ES_SEARCH_BUS_ADDRESS);

                    LOGGER.debug("ElasticSearch Service available at address {}", ElasticSearchService.ES_SEARCH_BUS_ADDRESS);

                    final DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(configData);

                    vertx.deployVerticle(BugHunterVerticle.class.getName(), deploymentOptions);
                } else {
                    LOGGER.warn("Bug Hunter not deployed as no config available, got config {}", configData);
                }

            } else {
                LOGGER.error("Error while loading Config", bugHunterConfig.cause());
            }
        });

        //TODO handle configmap change
    }
}
