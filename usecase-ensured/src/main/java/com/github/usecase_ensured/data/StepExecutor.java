package com.github.usecase_ensured.data;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class StepExecutor {

    public void run(Context steps) {
        steps.getEntries().forEach(step -> executeAndAssert(step, steps.getSavedVariables()));
    }

    private void executeAndAssert(TestStep step, Map<String, JsonNode> context) {
        var builder = given();

        builder.baseUri(step.request().url());

        if (step.request().body() != null) {
            builder.body(step.request().body());
        }

        builder.headers(new Headers(step.request().headers()));
        builder.contentType(ContentType.JSON);
        builder.accept(ContentType.JSON);

        var response = builder.request(step.request().method().name());

        if (step.expectedResponse() != null) {
            var expectedStatusCode = step.expectedResponse().expectedResponse().at("/response");
            if (!expectedStatusCode.isMissingNode()) {
                var msg = "invalid HTTP status: " + step.asTraceHint();
                assertEquals(expectedStatusCode.asInt(), response.statusCode(), msg);
                response.then().statusCode(expectedStatusCode.asInt());
            }

            var expected = step.expectedResponse().expectedResponse().at("/content");
            if (expected != null && !expected.isMissingNode()) {
                var actual = response.body().as(JsonNode.class);
                var fieldAssertions = createFieldAssertions(expected, actual, step);
                fieldAssertions.forEach(Runnable::run);
            }
        }
    }

    private List<Runnable> createFieldAssertions(JsonNode expectedNode, JsonNode actualNode, TestStep step) {
        var acc = new ArrayList<Runnable>();
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
                    fail("%s: unexpected fields %s present".formatted(step.asTraceHint(), unexpectedFields))
            );
        }

        actualNode.properties().forEach(entry -> {
            var fieldName = entry.getKey();
            var actual = entry.getValue();
            var expected = expectedNode.at("/" + fieldName);
            var metaAssertion = metaVariableAssertion(expected, actualNode, fieldName, step);

            if (metaAssertion == null) {
                acc.add(() -> assertEquals(expected, actual, step.asTraceHint(fieldName, actualNode)));
            } else {
                acc.add(metaAssertion);
            }
        });

        return acc;
    }

    private Runnable metaVariableAssertion(JsonNode expected, JsonNode actualJson,
                                           String actualFieldName, TestStep step) {
        if (expected.isTextual() && "{{any}}".equals(expected.asText())) {
            return () -> assertTrue(true, step.asTraceHint(actualFieldName, actualJson));
        }
        return null;
    }

}
