package com.github.usecase_ensured.internal;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.http.Headers;

public abstract class Request {
    protected final JsonNode request;

    protected Request(JsonNode request) {
        this.request = request;
    }

    public JsonNode request() {
        return request;
    }

    public abstract String url();
    public abstract String body();
    public abstract Headers headers();
    public abstract String method();

    public enum Method {
        GET, POST, PUT, DELETE
    }
}
