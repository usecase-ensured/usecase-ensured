package com.github.usecase_ensured.internal;

import io.restassured.http.Header;

import java.util.List;

public abstract class Request {
    protected final Method method;
    protected final List<Header> headers;
    protected final String url;
    protected final String body;

    protected Request(Method method, List<Header> headers, String url, String body) {
        this.method = method;
        this.headers = headers;
        this.url = url;
        this.body = body;
    }

    public Method method() {
        return method;
    }

    public List<Header> headers() {
        return headers;
    }

    public String url() {
        return url;
    }

    public String body() {
        return body;
    }

    public enum Method {
        GET, POST, PUT, DELETE
    }
}
