/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.fabric8.devops.apps.elasticsearch.helper.service;

import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link ElasticSearchOptions}.
 *
 */
public class ElasticSearchOptionsConverter {

  public static void fromJson(JsonObject json, ElasticSearchOptions obj) {
    if (json.getValue("ELASTIC_SEARCH_SERVICE_NAME") instanceof String) {
      obj.setHost((String)json.getValue("ELASTIC_SEARCH_SERVICE_NAME"));
    }
    if (json.getValue("ELASTIC_SEARCH_INDEXES") instanceof String) {
      obj.setIndexes((String)json.getValue("ELASTIC_SEARCH_INDEXES"));
    }
    if (json.getValue("ELASTIC_SEARCH_SERVICE_PORT") instanceof Number) {
      obj.setPort(((Number)json.getValue("ELASTIC_SEARCH_SERVICE_PORT")).intValue());
    }
    if (json.getValue("ssl") instanceof Boolean) {
      obj.setSsl((Boolean)json.getValue("ssl",false));
    }
  }

  public static void toJson(ElasticSearchOptions obj, JsonObject json) {
    if (obj.getHost() != null) {
      json.put("ELASTIC_SEARCH_SERVICE_NAME", obj.getHost());
    }
    if (obj.getIndexes() != null) {
      json.put("ELASTIC_SEARCH_INDEXES", obj.getIndexes());
    }
    json.put("ELASTIC_SEARCH_SERVICE_PORT", obj.getPort());
    json.put("ssl", obj.isSsl());
  }
}
