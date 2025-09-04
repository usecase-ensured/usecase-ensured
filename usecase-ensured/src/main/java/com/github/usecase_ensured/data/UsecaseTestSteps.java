package com.github.usecase_ensured.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UsecaseTestSteps implements TestSteps {
    private final List<TestStep> testSteps;
    private final Map<String, JsonNode> savedMap = new HashMap<>();
    private final ObjectMapper MAPPER = new ObjectMapper();

    public UsecaseTestSteps(Path path) {
        testSteps = buildStepsFromUsecase(path);
    }

    public List<TestStep> getEntries() {
        return testSteps;
    }

    @Override
    public Map<String, JsonNode> getSavedVariables() {
        return Map.copyOf(savedMap);
    }

    @Override
    public void updateSavedVariables(String name, JsonNode value) {
        savedMap.put(name, value);
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
            var headersNode = Optional.ofNullable(entry.at("/given/headers"));

            var headers = headersNode.map(it -> it.propertyStream()
                            .map(header -> new Header(
                                    header.getKey(),
                                    header.getValue().asText()
                            ))
                            .toList())
                    .orElse(List.of());

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

            if (entry.hasNonNull("/remembered")) {
                var rememberedValues = entry.at("/remembered");
                for (var variable : rememberedValues.properties()) {
                    savedMap.put(variable.getKey(), variable.getValue());
                }
            }

            var step = new TestStep(path, name, request, parsedExpectedJson);
            steps.add(step);
        }

        return steps;
    }

}
