package com.github.usecase_ensured.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface Context {
    List<TestStep> getEntries();
    Map<String, JsonNode> getSavedVariables();
    void updateSavedVariables(String name, JsonNode value);
}
