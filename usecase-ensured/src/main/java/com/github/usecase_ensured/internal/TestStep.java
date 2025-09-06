package com.github.usecase_ensured.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.github.usecase_ensured.internal.runner.Context;
import io.restassured.response.Response;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public record TestStep(
        Path filePath,
        String name,
        Request request,
        ExpectedResponse expectedResponse
) {
    public String asTraceHint() {
        return "STEP [%s] FILE [%s]".formatted(name, filePath);
    }

    public String asTraceHint(String fieldName, JsonNode json) {

        var firstLine = "STEP [%s] FIELD [%s] FILE [%s]".formatted(name, fieldName, filePath);
        var s = json.toPrettyString();
        var newS = new ArrayList<String>();
        newS.add(firstLine);

        for (var line : s.lines().toList()) {
            var trimmedLine = line.trim();
            if (trimmedLine.startsWith("\"%s\"".formatted(fieldName))) {
                newS.add(line + "    <<<<<<< HERE IS THE PROBLEM");
            } else {
                newS.add(line);
            }
        }

        return """
               %s
               """.formatted(String.join(System.lineSeparator(), newS));
    }

    public void assertOn(Response response, Context context) {
        if (expectedResponse() != null) {
            var expectedStatusCode = expectedResponse().expectedResponse().optional("statusCode");
            if (expectedStatusCode.isPresent()) {
                var msg = "invalid HTTP status: " + asTraceHint();
                assertEquals(expectedStatusCode.get().asInt(), response.statusCode(), msg);
            }

            var expected = expectedResponse().expectedResponse().optional("body");
            if (expected.isPresent()) {
                var actual = response.body().as(JsonNode.class);
                var fieldAssertions = createFieldAssertions(expected.get(), actual);
                fieldAssertions.forEach(Assertion::run);
            }
        }
    }

    @FunctionalInterface
    private interface Assertion extends Runnable {
    }

    private List<Assertion> createFieldAssertions(JsonNode expectedNode, JsonNode actualNode) {
        if (expectedNode.isValueNode()) {
            return List.of(() -> assertEquals(expectedNode, actualNode, this.asTraceHint()));
        }
        var acc = new ArrayList<Assertion>();
        var actualValueMap = new HashMap<String, JsonNode>();

        actualNode.properties().forEach(entry ->
                actualValueMap.put(entry.getKey(), entry.getValue())
        );

        var expectedValueMap = new HashMap<String, JsonNode>();
        expectedNode.properties().forEach(entry ->
                expectedValueMap.put(entry.getKey(), entry.getValue())
        );

        var unexpectedFields = new HashSet<>(expectedValueMap.keySet());
        unexpectedFields.removeAll(actualValueMap.keySet());

        var extraFields = new HashSet<>(actualValueMap.keySet());
        extraFields.removeAll(expectedValueMap.keySet());
        unexpectedFields.addAll(extraFields);

        if (!unexpectedFields.isEmpty()) {
            return List.of(() ->
                    fail("%s: unexpected fields %s present".formatted(this.asTraceHint(), unexpectedFields))
            );
        }

        actualNode.properties().forEach(entry -> {
            var fieldName = entry.getKey();
            var actual = entry.getValue();
            var expected = expectedNode.at("/" + fieldName);
            var metaAssertion = metaVariableAssertion(expected, actualNode, fieldName);

            if (metaAssertion == null) {
                acc.add(() -> assertEquals(expected, actual, this.asTraceHint(fieldName, actualNode)));
            } else {
                acc.add(metaAssertion);
            }
        });

        return acc;
    }

    private Assertion metaVariableAssertion(JsonNode expected, JsonNode actualJson, String actualFieldName) {
        if (expected.isTextual() && "{{any}}".equals(expected.asText())) {
            return () -> assertTrue(true, this.asTraceHint(actualFieldName, actualJson));
        }
        return null;
    }

}
