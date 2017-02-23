package io.fabric8.devops.apps.bughunter.service.impl;

import io.fabric8.devops.apps.bughunter.model.BugInfo;
import io.fabric8.devops.apps.bughunter.service.LogsAnalyzerService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author kameshs
 */
public class ExceptionsLogsAnalyzer implements LogsAnalyzerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionsLogsAnalyzer.class);

    //TODO for future use
    private final Vertx vertx;

    public ExceptionsLogsAnalyzer(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void analyze(JsonArray hits, Handler<AsyncResult<JsonObject>> resultHandler) {

        LOGGER.debug("Analyzing Exceptions ...");

        final JsonObject jBugInfo = new JsonObject();

        final JsonArray jBugInfos = new JsonArray();

        final List<Optional<BugInfo>> bugInfos = hits.stream()
            .map(JsonObject.class::cast)
            .map(BugHitsAnalyzer::process)
            .collect(Collectors.toList());

        LOGGER.info("Collected {} bug hits", bugInfos.size());

        bugInfos.stream()
            .filter(o -> o.isPresent())
            .map(o -> o.get().toJson())
            .forEach(o -> jBugInfos.add(o));

        jBugInfo.put("bugs", jBugInfos);

        resultHandler.handle(Future.succeededFuture(jBugInfo));

    }
}
