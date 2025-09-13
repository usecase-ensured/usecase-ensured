package com.github.usecase_ensured.internal.runner.postman;

import com.github.usecase_ensured.internal.Request;
import io.restassured.http.Header;

import java.util.List;

public class PostmanRequest extends Request {
    public PostmanRequest(Method method, List<Header> headers, String url, String body) {
        super(method, headers, url, body);
    }
}
