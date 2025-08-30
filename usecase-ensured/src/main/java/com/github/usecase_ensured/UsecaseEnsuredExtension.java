package com.github.usecase_ensured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.github.usecase_ensured.data.ExpectedResponse;
import com.github.usecase_ensured.data.Request;
import com.github.usecase_ensured.data.TestStep;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Enables the Usecase Ensured functionality in the annotated JUnit test class.
 */
public class UsecaseEnsuredExtension implements BeforeTestExecutionCallback {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        var maybeMethod = context.getTestMethod();

        if (maybeMethod.isPresent()) {
            var method = maybeMethod.get();
            var annotation = method.getAnnotation(Usecase.class);

            if (annotation == null) return;

            var fileType = annotation.type();
            var filePath = fileType.pathPrefix.resolve(annotation.value());

            var steps = switch (fileType) {
                case POSTMAN -> buildStepsFromPostman(filePath);
                case USECASE -> buildStepsFromUsecase(filePath);
            };

            for (var step : steps) {
                doRequest(step);
            }
        }
    }

    private void doRequest(TestStep step) {
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

    private void ensureThatPathPointsToFile(Path path) {
        if (Files.notExists(path)) {
            throw new RuntimeException("%s is not a valid file".formatted(path));
        }

        if (!Files.isRegularFile(path)) {
            throw new RuntimeException("%s must be an actual, simple file".formatted(path));
        }
    }

    private List<TestStep> buildStepsFromUsecase(Path path) {
        ensureThatPathPointsToFile(path);

        JsonNode json;
        try {
            json = MAPPER.reader().readTree(new FileInputStream(path.toFile()));
        } catch (IOException e) {
            throw new RuntimeException("failed to parse file '%s'. invalid JSON".formatted(path), e);
        }
        var entries = json.get("steps");
        if (!entries.isArray()) {
            throw new RuntimeException("'steps' is supposed to be an array!!!");
        }

        var steps = new ArrayList<TestStep>();

        for (var entry : entries) {
            var name = entry.requiredAt("/name").asText();
            var method = entry.requiredAt("/given/method").asText();
            var headersNode = entry.requiredAt("/given/headers");

            var headers = headersNode.propertyStream()
                    .map(header -> new Header(
                            header.getKey(),
                            header.getValue().asText()
                    ))
                    .toList();

            var url = entry.requiredAt("/given/url").asText();
            var bodyNode = entry.at("/given/body");
            var body = bodyNode.isMissingNode() ? null : bodyNode.toPrettyString();

            var request = new Request(
                    Request.Method.valueOf(method),
                    headers,
                    url,
                    body
            );

            var expectationNode = entry.requiredAt("/expected");

            var parsedExpectedJson = new ExpectedResponse(expectationNode);

            var step = new TestStep(path, name, request, parsedExpectedJson);
            steps.add(step);
        }

        return steps;
    }

    private List<TestStep> buildStepsFromPostman(Path path) {
        ensureThatPathPointsToFile(path);

        JsonNode json;
        try {
            json = MAPPER.reader().readTree(new FileInputStream(path.toFile()));
        } catch (IOException e) {
            throw new RuntimeException("failed to parse file '%s'. invalid JSON".formatted(path), e);
        }

        var entries = json.get("item");
        assert entries.isArray() : "this is supposed to be a JSON array";

        var steps = new ArrayList<TestStep>();

        for (var entry : entries) {
            var name = entry.requiredAt("/name").asText();
            var method = entry.requiredAt("/request/method").asText();
            var headersNode = entry.requiredAt("/request/header");

            var headers = StreamSupport.stream(headersNode.spliterator(), false)
                    .map(headerNode -> new Header(
                            headerNode.requiredAt("/key").asText(),
                            headerNode.requiredAt("/value").asText()
                    ))
                    .toList();

            var url = entry.requiredAt("/request/url/raw").asText();
            var bodyNode = entry.at("/request/body/raw");
            var body = bodyNode.isMissingNode() ? null : bodyNode.asText();

            var request = new Request(
                    Request.Method.valueOf(method),
                    headers,
                    url,
                    body
            );

            var expectationNode = entry.requiredAt("/event/0/script/exec");
            var expectation = StreamSupport.stream(expectationNode.spliterator(), false)
                    .map(JsonNode::asText)
                    .reduce(String::concat)
                    .orElse("")
                    .replaceAll(" +", " ");

            var jsonValue = expectation.split("=")[1].trim();

            ExpectedResponse parsedExpectedJson;
            try {
                parsedExpectedJson = new ExpectedResponse(MAPPER.reader().readTree(jsonValue));
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse expected JSON", e);
            }

            var step = new TestStep(path, name, request, parsedExpectedJson);
            steps.add(step);
        }

        return steps;
    }
}
  