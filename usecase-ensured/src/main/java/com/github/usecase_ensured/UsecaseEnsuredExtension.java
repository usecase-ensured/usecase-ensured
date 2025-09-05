package com.github.usecase_ensured;

import com.github.usecase_ensured.data.PostmanContext;
import com.github.usecase_ensured.data.StepExecutor;
import com.github.usecase_ensured.data.UsecaseContext;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Enables the Usecase Ensured functionality in the annotated JUnit test class.
 */
public class UsecaseEnsuredExtension implements BeforeTestExecutionCallback {
    private static final StepExecutor stepExecutor = new StepExecutor();

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        var maybeMethod = context.getTestMethod();

        if (maybeMethod.isPresent()) {
            var method = maybeMethod.get();
            var annotation = method.getAnnotation(Usecase.class);

            if (annotation == null) {
                return;
            }

            var fileType = annotation.type();
            var filePath = fileType.pathPrefix.resolve(annotation.value());

            var steps = switch (fileType) {
                case POSTMAN -> new PostmanContext(filePath);
                case USECASE -> new UsecaseContext(filePath);
            };

            stepExecutor.run(steps);
        }
    }

}
  