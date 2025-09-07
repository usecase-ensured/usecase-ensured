package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.usecase_ensured.internal.ExpectedResponse;
import com.github.usecase_ensured.internal.Request;
import com.github.usecase_ensured.internal.TestStep;

import java.nio.file.Path;
import java.util.Optional;

class UsecaseStep extends TestStep {
    private UsecaseContext context;

    UsecaseStep(UsecaseContext context,
                       Path filePath,
                       String name,
                       Request request,
                       ExpectedResponse expectedResponse) {
        super(filePath, name, request, expectedResponse);
        this.context = context;
    }

    @Override
    protected Optional<JsonNode> expectedStatusCodeJsonNode() {
        return expectedResponse.expectedResponse().optional("statusCode");
    }

    @Override
    protected Optional<JsonNode> expectedBodyJsonNode() {
        return expectedResponse.expectedResponse().optional("body");
    }

    @Override
    protected void updateSavedMetaVariables(JsonNode actualResponse) {

    }
}
