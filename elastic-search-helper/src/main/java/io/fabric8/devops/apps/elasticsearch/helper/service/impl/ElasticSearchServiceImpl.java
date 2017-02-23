package io.fabric8.devops.apps.elasticsearch.helper.service.impl;

import io.fabric8.devops.apps.elasticsearch.helper.service.ElasticSearchOptions;
import io.fabric8.devops.apps.elasticsearch.helper.service.ElasticSearchService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author kameshs
 */
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

    //TODO - right error codes with desc
    private static final int ESQ1001 = 1001; //Error while building request
    private static final int ESQ1002 = 1002; //Error while doing ES search

    private final Vertx vertx;

    private ElasticSearchOptions elasticSearchOptions;

    public ElasticSearchServiceImpl(Vertx vertx) {
        this(vertx, new ElasticSearchOptions());
    }

    public ElasticSearchServiceImpl(Vertx vertx, JsonObject esConfig) {
        this(vertx, new ElasticSearchOptions(esConfig));
    }

    public ElasticSearchServiceImpl(Vertx vertx, ElasticSearchOptions elasticSearchOptions) {
        this.vertx = vertx;
        this.elasticSearchOptions = elasticSearchOptions;
    }

    //TODO add some error context info
    @Override
    public void search(String searchQuery, Handler<AsyncResult<JsonObject>> resultHandler) {

        boolean isSsl = elasticSearchOptions.isSsl();
        String esServiceName = elasticSearchOptions.getHost();
        int esServicePort = elasticSearchOptions.getPort();

        String indexes = elasticSearchOptions.getIndexes();

        LOGGER.debug("Searching with q={} on indexes {}", searchQuery, indexes);

        HttpClientOptions httpClientOptions = createHttpClientOptions(isSsl, esServiceName, esServicePort);
        query(searchQuery, indexes, resultHandler, httpClientOptions);
    }

    @Override
    public void save(String index, String type, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {

        LOGGER.debug("Saving Data to Index {}", index);

        boolean isSsl = elasticSearchOptions.isSsl();
        String esServiceName = elasticSearchOptions.getHost();
        int esServicePort = elasticSearchOptions.getPort();

        final String uri = "/" + index + "/" + type + "/" + UUID.randomUUID();

        HttpClientOptions httpClientOptions = createHttpClientOptions(isSsl, esServiceName, esServicePort);
        elasticSearchSaveEndpoint(httpClientOptions, req -> {

            req.result().sendJsonObject(data, response -> {
                if (response.succeeded()) {
                    LOGGER.debug("Successfully saved data to  index {}", index);
                    resultHandler.handle(Future.succeededFuture(response.result().bodyAsJsonObject()));
                } else {
                    LOGGER.error("Error saving data to index " + index, response.cause());
                    //TODO Convert this to Service Exception
                    resultHandler.handle(Future.failedFuture(response.cause()));
                }
            });

        }, uri);
    }

    /**
     * @param isSsl
     * @param esServiceName
     * @param esServicePort
     * @return
     */
    protected HttpClientOptions createHttpClientOptions(boolean isSsl, String esServiceName, int esServicePort) {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setSsl(isSsl);
        httpClientOptions.setDefaultHost(esServiceName);
        httpClientOptions.setDefaultPort(esServicePort);
        return httpClientOptions;
    }

    /**
     * @param httpClientOptions
     * @param queryRequestHandler
     * @param searchQuery
     */
    protected void elasticSearchEndpoint(HttpClientOptions httpClientOptions,
                                         Handler<AsyncResult<HttpRequest<Buffer>>> queryRequestHandler,
                                         String uri,
                                         String searchQuery) {
        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);
        WebClient webClient = WebClient.wrap(httpClient);
        HttpRequest<Buffer> httpRequest = webClient.get(uri).addQueryParam("q", searchQuery);
        //TODO add request in cache and reuse it
        queryRequestHandler.handle(Future.succeededFuture(httpRequest));
    }

    /**
     * @param httpClientOptions
     * @param saveRequestHandler
     */
    protected void elasticSearchSaveEndpoint(HttpClientOptions httpClientOptions,
                                             Handler<AsyncResult<HttpRequest<Buffer>>> saveRequestHandler,
                                             String uri) {
        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);
        WebClient webClient = WebClient.wrap(httpClient);
        HttpRequest<Buffer> httpRequest = webClient.put(uri);
        //TODO add request in cache and reuse it
        saveRequestHandler.handle(Future.succeededFuture(httpRequest));
    }


    /**
     * @param searchQuery
     * @param indexes
     * @param resultHandler
     * @param httpClientOptions
     */
    private void query(String searchQuery, String indexes, Handler<AsyncResult<JsonObject>> resultHandler,
                       HttpClientOptions httpClientOptions) {

        final String uri = indexes + "/_search";

        LOGGER.debug("Search URI:{}", uri);

        elasticSearchEndpoint(httpClientOptions, searchResult -> {

            if (searchResult.succeeded()) {
                HttpRequest<Buffer> bufferHttpRequest = searchResult.result();
                bufferHttpRequest.send(response -> {
                    if (response.succeeded()) {
                        JsonObject searchResponse = response.result().bodyAsJsonObject();
                        resultHandler.handle(Future.succeededFuture(searchResponse));
                    } else {
                        LOGGER.error("Error while searching ", response.cause());
                        resultHandler.handle(ServiceException.fail(ESQ1002, response.cause().getMessage()));
                    }
                });
            } else {
                LOGGER.error("Error while building request ", searchResult.cause());
                resultHandler.handle(ServiceException.fail(ESQ1001, searchResult.cause().getMessage()));
            }
        }, uri, searchQuery);
    }
}
