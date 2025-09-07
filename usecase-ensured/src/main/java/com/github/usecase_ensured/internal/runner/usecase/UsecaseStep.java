package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.usecase_ensured.internal.ExpectedResponse;
import com.github.usecase_ensured.internal.Request;
import com.github.usecase_ensured.internal.TestStep;

import java.nio.file.Path;
import java.util.Optional;

public class UsecaseStep extends TestStep {

    public UsecaseStep(Path filePath, String name, Request request, ExpectedResponse expectedResponse) {
        super(filePath, name, request, expectedResponse);
    }

    @Override
    public Optional<JsonNode> expectedStatusCodeJsonNode() {
        return expectedResponse.expectedResponse().optional("statusCode");
    }

    @Override
    public Optional<JsonNode> expectedBodyJsonNode() {
        return expectedResponse.expectedResponse().optional("body");
    }
}
