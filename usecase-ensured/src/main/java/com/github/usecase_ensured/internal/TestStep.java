package com.github.usecase_ensured.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.usecase_ensured.internal.runner.Context;
import io.restassured.response.Response;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TestStep {
    protected final Path filePath;
    protected final String name;
    protected final Request request;
    protected ExpectedResponse expectedResponse;

    public Path filePath() {
        return filePath;
    }

    public String name() {
        return name;
    }

    public Request request() {
        return request;
    }

    public ExpectedResponse expectedResponse() {
        return expectedResponse;
    }

    public TestStep(Path filePath, String name, Request request, ExpectedResponse expectedResponse) {
        this.filePath = filePath;
        this.name = name;
        this.request = request;
        this.expectedResponse = expectedResponse;
    }

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

    protected abstract Optional<JsonNode> expectedStatusCodeJsonNode();

    protected abstract Optional<JsonNode> expectedBodyJsonNode();

    protected void updateSavedMetaVariables(JsonNode actualResponse) {
    }

    protected void resolveMetaVariables() {
    }

    public void assertOn(Response response, Context context) {
        if (expectedResponse != null) {
            var expectedStatusCode = expectedStatusCodeJsonNode();
            if (expectedStatusCode.isPresent()) {
                var msg = "invalid HTTP status: " + asTraceHint();
                assertEquals(expectedStatusCode.get().asInt(), response.statusCode(), msg);
            }

            var expected = expectedBodyJsonNode();
            if (expected.isPresent()) {
                var actual = response.body().as(JsonNode.class);

                updateSavedMetaVariables(actual);
                resolveMetaVariables();

                for (var assertion : generateAssertions(expected.get(), actual)) {
                    assertion.execute();
                }
            }
        }
    }

    @FunctionalInterface
    protected interface Assertion {
        void execute();
    }

    protected List<Assertion> generateAssertions(JsonNode expectedNode, JsonNode actualNode) {
        if (expectedNode.isValueNode()) {
            return List.of(() -> assertEquals(expectedNode, actualNode, this.asTraceHint()));
        }

        var assertions = new ArrayList<Assertion>();

        var actualValueMap = new HashMap<String, JsonNode>();
        actualNode.properties().forEach(entry ->
                actualValueMap.put(entry.getKey(), entry.getValue())
        );

        var expectedValueMap = new HashMap<String, JsonNode>();
        expectedNode.properties().forEach(entry ->
                expectedValueMap.put(entry.getKey(), entry.getValue())
        );

        var fieldsNotPresentInActualResponse = new HashSet<>(expectedValueMap.keySet());
        fieldsNotPresentInActualResponse.removeAll(actualValueMap.keySet());

        var fieldsNotPresentInExpectation = new HashSet<>(actualValueMap.keySet());
        fieldsNotPresentInExpectation.removeAll(expectedValueMap.keySet());

        var unexpectedFields = new HashSet<String>();
        unexpectedFields.addAll(fieldsNotPresentInExpectation);
        unexpectedFields.addAll(fieldsNotPresentInActualResponse);

        if (!unexpectedFields.isEmpty()) {
            return List.of(
                    () -> fail("%s: unexpected fields %s present".formatted(this.asTraceHint(), unexpectedFields)));
        }

        actualNode.properties().forEach(entry -> {
            var fieldName = entry.getKey();
            var actual = entry.getValue();
            var expected = expectedNode.at("/" + fieldName);
            var metaAssertion = metaVariableAssertion(expected, actualNode, fieldName);

            if (metaAssertion == null) {
                assertions.add(() -> assertEquals(expected, actual, this.asTraceHint(fieldName, actualNode)));
            } else {
                assertions.add(metaAssertion);
            }
        });

        return assertions;
    }

    protected Assertion metaVariableAssertion(JsonNode expected, JsonNode actualJson, String actualFieldName) {
        if (expected.isTextual() && "{{any}}".equals(expected.asText())) {
            return () -> assertTrue(true, this.asTraceHint(actualFieldName, actualJson));
        }
        return null;
    }

}
