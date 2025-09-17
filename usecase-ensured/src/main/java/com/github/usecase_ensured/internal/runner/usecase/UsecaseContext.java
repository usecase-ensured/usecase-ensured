package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.usecase_ensured.internal.TestStep;
import com.github.usecase_ensured.internal.runner.Context;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class UsecaseContext extends Context {
    public List<TestStep> steps;
    private final Map<String, JsonNode> savedMetaVariables = new HashMap<>();
    private final Map<String, JsonNode> givenMetaVariables = new HashMap<>();
    private static final Pattern metaVariableRegex = Pattern.compile("\\{\\{(?:(?!\\{\\{).)*}}");

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
            var expected = usecaseStep.optional("then").orElse(NullNode.instance);

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

    protected JsonNode resolve(JsonNode expectedResponse) {
        if (Helper.looksLikeMetaVariable(expectedResponse)) {
            return getVariable(expectedResponse.textValue());
        } else if (Helper.containsMetaVariable(expectedResponse)) {
            var resolvedString = resolveMetaVariablePartsOf((TextNode) expectedResponse);
            return TextNode.valueOf(resolvedString);
        } else {
            return replaceMetaVariables(expectedResponse);
        }

    }

    private JsonNode replaceMetaVariables(JsonNode expectedResponse) {
        if (expectedResponse.isObject()) {
            var parentNode = (ObjectNode) expectedResponse;
            for (var prop : parentNode.properties()) {
                var propValue = prop.getValue();
                if (Helper.looksLikeMetaVariable(propValue)) {
                    var resolvedValue = getVariable(propValue.textValue());
                    parentNode.replace(prop.getKey(), resolvedValue);
                } else if (Helper.containsMetaVariable(propValue)) {
                    var resolvedString = resolveMetaVariablePartsOf((TextNode) propValue);
                    parentNode.replace(prop.getKey(), TextNode.valueOf(resolvedString));
                } else if (propValue.isObject()) {
                    parentNode.replace(prop.getKey(), replaceMetaVariables(propValue));
                }
            }
        }
        return expectedResponse;
    }

    public JsonNode getVariable(String metaVariable) {
        if (metaVariable.equals("{{any}}")) {
            return TextNode.valueOf("{{any}}");
        }

        var parts = trimBraces(metaVariable).split("\\.", 2);

        var invalidMetaVariablePath = new RuntimeException("invalid meta variable classifier %s".formatted(parts[0]));

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

    public String resolveMetaVariablePartsOf(TextNode urlNode) {
        var resolvedString = new StringBuilder();
        String url = urlNode.asText();
        var metaVariablesAsSeparators = url.splitWithDelimiters(metaVariableRegex.pattern(), -1);

        for (var part : metaVariablesAsSeparators) {
            if (Helper.isMetaVariable(part)) {
                resolvedString.append(getVariable(part).asText());
            } else {
                resolvedString.append(part);
            }
        }
        return resolvedString.toString();
    }

    public static class Helper {

        public static boolean containsMetaVariable(JsonNode json) {
            return json.isTextual() && metaVariableRegex.matcher(json.textValue()).find();
        }

        public static Boolean looksLikeMetaVariable(JsonNode json) {
            return json.isTextual()
                    && json.textValue().startsWith("{{")
                    && json.textValue().endsWith("}}");
        }

        public static boolean isMetaVariable(String str) {
            return str.startsWith("{{") && str.endsWith("}}");
        }
    }
}
