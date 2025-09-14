package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.usecase_ensured.internal.ExpectedResponse;
import com.github.usecase_ensured.internal.TestStep;
import com.github.usecase_ensured.internal.runner.Context;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsecaseContext extends Context {
    public List<TestStep> steps;
    private final Map<String, JsonNode> savedMetaVariables = new HashMap<>();
    private final Map<String, JsonNode> givenMetaVariables = new HashMap<>();

    public UsecaseContext(Path sourceFile) {
        var usecaseJson = read(sourceFile);

        loadGivenMetaVariables(usecaseJson);
        steps = List.copyOf(prepareSteps(usecaseJson, sourceFile));
    }

    private void loadGivenMetaVariables(JsonNode json) {
        var givenNode = json.optional("given");

        if (givenNode.isPresent()) {
            for (var property : givenNode.get().properties()) {
                givenMetaVariables.put(property.getKey(), property.getValue());
            }
        }

    }

    private List<TestStep> prepareSteps(JsonNode json, Path sourceFile) {
        var steps = new ArrayList<TestStep>();

        for (var usecaseStep : json.required("steps")) {
            var name = usecaseStep.required("name").asText();

            var given = new UsecaseRequest(usecaseStep.required("do"), this);
            var expected = new ExpectedResponse(usecaseStep.optional("then").orElse(NullNode.instance));

            var step = new UsecaseStep(this, sourceFile, name, given, expected);
            steps.add(step);
        }

        return steps;
    }

    @Override
    public List<TestStep> steps() {
        return steps;
    }

    public void updateSavedVariables(String key, JsonNode responseValue) {
        savedMetaVariables.put(key, responseValue);
    }

    public JsonNode getVariable(String metaVariable) {
        if (metaVariable.equals("{{any}}")) {
            return TextNode.valueOf("{{any}}");
        }

        var parts = trimBraces(metaVariable).split("\\.", 2);

        var invalidMetaVariablePath = new RuntimeException("invalid meta variable classifier parts[0]");

        if (parts.length != 2) {
            throw invalidMetaVariablePath;
        }

        var metaVariableValue = switch (parts[0]) {
            case "saved" -> savedMetaVariables.get(parts[1]);
            case "given" -> givenMetaVariables.get(parts[1]);
            default -> throw invalidMetaVariablePath;
        };

        if (metaVariableValue == null) {
            throw new RuntimeException("undefined meta variable %s".formatted(metaVariableValue));
        }

        return metaVariableValue;
    }

    private String trimBraces(String metaVariable) {
        return metaVariable.substring(2, metaVariable.length() - 2);
    }

}
