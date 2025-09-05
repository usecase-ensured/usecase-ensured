package com.github.usecase_ensured.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class PostmanContext implements Context {
    private final List<TestStep> testSteps;
    private final ObjectMapper MAPPER = new ObjectMapper();

    public PostmanContext(Path path) {
        testSteps = buildStepsFromPostman(path);
    }

    public List<TestStep> getEntries() {
        return List.copyOf(testSteps);
    }

    @Override
    public Map<String, JsonNode> getSavedVariables() {
        return Map.of();
    }

    @Override
    public void updateSavedVariables(String name, JsonNode value) {

    }

    private void ensureThatPathPointsToFile(Path path) {
        if (Files.notExists(path)) {
            throw new RuntimeException("%s is not a valid file".formatted(path));
        }

        if (!Files.isRegularFile(path)) {
            throw new RuntimeException("%s must be an actual, simple file".formatted(path));
        }
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
