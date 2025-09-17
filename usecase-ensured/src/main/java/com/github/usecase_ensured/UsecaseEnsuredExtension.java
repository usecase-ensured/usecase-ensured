package com.github.usecase_ensured;

import com.github.usecase_ensured.internal.runner.Context;
import com.github.usecase_ensured.internal.runner.Runner;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Enables the Usecase Ensured functionality in the annotated JUnit test class.
 */
public class UsecaseEnsuredExtension implements BeforeTestExecutionCallback {
    private static final Runner runner = new Runner();

    @Override
    public void beforeTestExecution(ExtensionContext junitContext) {
        var maybeMethod = junitContext.getTestMethod();

        if (maybeMethod.isPresent()) {
            var method = maybeMethod.get();
            var annotation = method.getAnnotation(Usecase.class);

            if (annotation == null) return;

            var context = Context.configureWith(annotation);
            runner.runWith(context);
        }
    }
}
  