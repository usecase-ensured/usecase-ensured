package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.usecase_ensured.internal.ExpectedResponse;
import com.github.usecase_ensured.internal.TestStep;
import io.restassured.http.Header;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

class UsecaseStep extends TestStep {
    private final UsecaseContext context;

    UsecaseStep(UsecaseContext context,
                Path filePath,
                String name,
                UsecaseRequest request,
                ExpectedResponse expectedResponse) {
        super(filePath, name, request, expectedResponse);
        this.context = context;
    }

    @Override
    public List<Header> headers() {
        return List.of();
//        request.headers().stream().map(h -> new Header(h.n))
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
        var savedVariables = ((UsecaseRequest) request).savedVariables();

        for (var savedVariable : savedVariables.properties()) {
            if (looksLikeMetaVariable(savedVariable.getValue())) {
                String metaVariablePath = savedVariable.getValue().textValue();
                var jsonPath = asJsonPath(metaVariablePath);
                var responseValue = actualResponse.at(jsonPath);

                if (responseValue.isMissingNode()) {
                    throw new RuntimeException(
                            "invalid saved variable definition, reference is not valid: {{%s}}".formatted(
                                    metaVariablePath));
                }

                context.updateSavedVariables(savedVariable.getKey(), responseValue);
            } else {
                context.updateSavedVariables(savedVariable.getKey(), savedVariable.getValue());
            }
        }
    }

    @Override
    protected void resolveMetaVariables() {
        JsonNode expectedResponseValue = expectedResponse.expectedResponse();
        if (looksLikeMetaVariable(expectedResponseValue)) {
            expectedResponse = new ExpectedResponse(context.getVariable(expectedResponseValue.textValue()));
        } else {
            replaceMetaVariables(expectedResponseValue);
        }

    }

    private void replaceMetaVariables(JsonNode expectedResponse) {
        if (expectedResponse.isObject()) {
            var parentNode = (ObjectNode) expectedResponse;
            for (var prop : parentNode.properties()) {
                var propValue = prop.getValue();
                if (looksLikeMetaVariable(propValue)) {
                    var resolvedValue = context.getVariable(propValue.textValue());
                    parentNode.replace(prop.getKey(), resolvedValue);
                } else if (propValue.isObject()) {
                    replaceMetaVariables(propValue);
                }
            }
        }
    }

    private String asJsonPath(String metaVariable) {
        return "/" + metaVariable.replace(".", "/");
    }

    private Boolean looksLikeMetaVariable(JsonNode json) {
        return json.isTextual()
                && json.textValue().startsWith("{{")
                && json.textValue().endsWith("}}");
    }
}
