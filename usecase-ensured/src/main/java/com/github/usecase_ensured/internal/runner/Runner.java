package com.github.usecase_ensured.internal.runner;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class Runner {
    public void runWith(Context context) {
        for (var step: context.steps()) {
            var requestBuilder = given().baseUri(step.request().url());

            if (step.request().body() != null) {
                requestBuilder.body(step.request().body());
                requestBuilder.contentType(ContentType.JSON);
            }
            requestBuilder.accept(ContentType.JSON);
            requestBuilder.headers(step.request().headers());

            var response = requestBuilder.request(step.request().method());

            step.assertOn(response);
        }

    }
}
