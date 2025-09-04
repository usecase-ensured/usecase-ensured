package com.github.usecase_ensured.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public class TestSteps {
    private final List<TestStep> testSteps;
    private final Map<String, JsonNode> rememberedMap;

    public TestSteps(List<TestStep> steps, Map<String, JsonNode> rememberedMap) {
        testSteps = List.copyOf(steps);
        this.rememberedMap = Map.copyOf(rememberedMap);
    }

    public List<TestStep> getEntries() {
        return testSteps;
    }
}
