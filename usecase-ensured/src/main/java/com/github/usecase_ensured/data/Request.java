package com.github.usecase_ensured.data;

import io.restassured.http.Header;

import java.util.List;

public record Request(
        Method method,
        List<Header> headers,
        String url,
        String body
) {

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}
