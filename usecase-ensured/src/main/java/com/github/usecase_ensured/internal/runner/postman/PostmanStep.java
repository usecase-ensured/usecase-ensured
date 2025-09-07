package com.github.usecase_ensured.internal.runner.postman;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.usecase_ensured.internal.ExpectedResponse;
import com.github.usecase_ensured.internal.Request;
import com.github.usecase_ensured.internal.TestStep;
import com.github.usecase_ensured.internal.runner.Context;
import io.restassured.response.Response;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PostmanStep extends TestStep {

    public PostmanStep(Path filePath, String name, Request request, ExpectedResponse expectedResponse) {
        super(filePath, name, request, expectedResponse);
    }

    @Override
    public Optional<JsonNode> expectedStatusCodeJsonNode() {
        return expectedResponse.expectedResponse().optional("response");
    }

    @Override
    public Optional<JsonNode> expectedBodyJsonNode() {
        return expectedResponse.expectedResponse().optional("content");
    }
}
