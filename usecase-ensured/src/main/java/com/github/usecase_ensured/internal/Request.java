package com.github.usecase_ensured.internal;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.http.Header;

import java.util.List;

public record Request(Method method, List<Header> headers, String url, String body) {
    public Request(JsonNode json) {
        this(Method.valueOf(json.requiredAt("/method").asText()),
                readHeaders(json.at("/headers")),
                json.required("url").asText(),
                json.optional("body").map(JsonNode::toPrettyString).orElse(null)
        );
    }

    private static List<Header> readHeaders(JsonNode json) {
        if (json.isMissingNode()) {
            return List.of();
        }
        return json.propertyStream()
                .map(it -> new Header(it.getKey(), it.getValue().asText()))
                .toList();
    }

    public enum Method {
        GET, POST, PUT, DELETE
    }
}
