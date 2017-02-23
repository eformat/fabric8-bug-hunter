package io.fabric8.devops.apps.elasticsearch.helper.service;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author kameshs
 */
@DataObject
public class ElasticSearchOptions {

    private boolean ssl;
    private String indexes;
    private String host;
    private int port;

    //TODO add ES search template

    public ElasticSearchOptions() {
        this.ssl = false;
        this.host = "localhost";
        this.port = 9200;
    }

    public ElasticSearchOptions(JsonObject json) {
        ElasticSearchOptionsConverter.fromJson(json, this);
    }

    public boolean isSsl() {
        return ssl;
    }

    public ElasticSearchOptions setSsl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ElasticSearchOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ElasticSearchOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public String getIndexes() {
        return indexes;
    }

    public ElasticSearchOptions setIndexes(String indexes) {
        this.indexes = indexes;
        return this;
    }


    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("host", host);
        jsonObject.put("port", port);
        jsonObject.put("indexes", indexes);
        return jsonObject;
    }
}
