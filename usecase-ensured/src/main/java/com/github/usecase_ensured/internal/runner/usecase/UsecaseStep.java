package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.usecase_ensured.internal.TestStep;

import java.nio.file.Path;
import java.util.Optional;

class UsecaseStep extends TestStep {
    private final UsecaseContext context;

    UsecaseStep(UsecaseContext context,
                Path filePath,
                String name,
                UsecaseRequest request,
                JsonNode expectedResponse) {
        super(filePath, name, request, expectedResponse);
        this.context = context;
    }

    @Override
    protected Optional<JsonNode> expectedStatusCode() {
        return expectedResponse.deepCopy().optional("statusCode");
    }

    @Override
    protected Optional<JsonNode> expectedBodyWithoutVariables(JsonNode actualBody) {
        updateSavedMetaVariables(actualBody);
        resolveMetaVariables();

        return expectedResponse.deepCopy().optional("body");
    }

    @Override
    protected void updateSavedMetaVariables(JsonNode actualResponse) {
        var savedVariables = ((UsecaseRequest) request).savedVariables();

        for (var savedVariable : savedVariables.properties()) {
            if (UsecaseContext.Helper.looksLikeMetaVariable(savedVariable.getValue())) {
                String metaVariablePath = savedVariable.getValue().textValue();
                var jsonPath = asJsonPath(withoutBraces(metaVariablePath));
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
        expectedResponse = context.resolve(expectedResponse);
    }

    private String asJsonPath(String metaVariable) {
        return "/" + metaVariable.replace(".", "/");
    }

    private String withoutBraces(String metaVariable) {
        return metaVariable.substring(2, metaVariable.length() - 2);
    }
}
