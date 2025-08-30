package com.github.usecase_ensured.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;
import java.util.ArrayList;

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
                newS.add(line + "    <<<<<<< VERIFY THE USECASE FILE. EITHER expected OR given VALUE IS WRONG");
            } else {
                newS.add(line);
            }
        }

        return """
               \n%s
               """.formatted(String.join(System.lineSeparator(), newS));
    }

}
