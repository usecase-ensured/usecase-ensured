package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.usecase_ensured.internal.Request;
import io.restassured.http.Header;

import java.util.List;

class UsecaseRequest extends Request {
    private final JsonNode savedVariables;
    protected UsecaseRequest(Method method, List<Header> headers, String url, String body, JsonNode savedVariables) {
        super(method, headers, url, body);
        this.savedVariables = savedVariables;
    }

    UsecaseRequest(JsonNode json) {
        this(Method.valueOf(json.requiredAt("/method").asText()),
                readHeaders(json.at("/headers")),
                json.required("url").asText(),
                json.optional("body").map(JsonNode::toPrettyString).orElse(null),
                json.at("/saved")
        );
    }

    public JsonNode savedVariables() {
        return savedVariables;
    }

    private static List<Header> readHeaders(JsonNode json) {
        if (json.isMissingNode()) {
            return List.of();
        }
        return json.propertyStream()
                .map(it -> new Header(it.getKey(), it.getValue().asText()))
                .toList();
    }

}
