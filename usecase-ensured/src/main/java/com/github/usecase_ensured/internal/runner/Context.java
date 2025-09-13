package com.github.usecase_ensured.internal.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.usecase_ensured.Usecase;
import com.github.usecase_ensured.internal.TestStep;
import com.github.usecase_ensured.internal.runner.usecase.UsecaseContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class Context {
    protected static final ObjectMapper mapper = new ObjectMapper();

    public abstract List<TestStep> steps();

    public static Context configureWith(Usecase usecase) {
        return switch (usecase.type()) {
            case POSTMAN -> throw new RuntimeException("NOOP");
            case USECASE -> new UsecaseContext(usecase.type().pathPrefix.resolve(usecase.value()));
        };
        
    }
    protected JsonNode read(Path path) {
        ensureThatPathPointsToFile(path);
        try {
            return mapper.reader().readTree(new FileInputStream(path.toFile()));
        } catch (IOException e) {
            throw new RuntimeException("failed to parse file '%s'. invalid JSON".formatted(path), e);
        }

    }
    private void ensureThatPathPointsToFile(Path path) {
        if (Files.notExists(path)) {
            throw new RuntimeException("%s is not a valid file".formatted(path));
        }

        if (!Files.isRegularFile(path)) {
            throw new RuntimeException("%s must be an actual, simple file".formatted(path));
        }
    }

}
