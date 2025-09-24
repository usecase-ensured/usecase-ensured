package com.github.usecase_ensured;

import java.lang.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This annotation is to be used on a test method.
 * <b>PREREQUISITE</b>: your app is expected to actually be running on a local port, this is an
 * integration test!
 * Displaying the {@code run} button in the IDE near the test method signature requires the usual
 * {@link org.junit.jupiter.api.Test} annotation.
 * <h2>File location & format</h2>
 * <p>The files need to be put into the {@code test/resources/usecase} directory in your project.</p>
 * <p>
 * The format is a JSON file that follow a certain convention.
 * The specification is made more dynamic with the help of magic strings, called meta variables.
 * Bellow is an example:
 * </p>
 * <pre>
 * {@code
 * {
 *   "name": "create and fetch with custom syntax",
 *   "given": {
 *     "baseUrl": "http://localhost:8080/dummy",
 *     "name": "Bob"
 *   },
 *   "steps": [
 *     {
 *       "name": "create",
 *       "do": {
 *         "method": "POST",
 *         "url": "{{given.baseUrl}}",
 *         "body": {
 *           "name": "{{given.name}}"
 *         },
 *         "saved": {
 *           "id": "{{id}}"
 *         }
 *       },
 *       "then": {
 *         "statusCode": 201,
 *         "body": {
 *           "name": "Bob",
 *           "id": "{{saved.id}}"
 *         }
 *       }
 *     },
 *     {
 *       "name": "fetch",
 *       "do": {
 *         "method": "GET",
 *         "url": "{{given.baseUrl}}/{{saved.id}}"
 *       },
 *       "then": {
 *         "statusCode": 200,
 *         "body": {
 *           "name": "Bob",
 *           "id": 0
 *         }
 *       }
 *     }
 *   ]
 * }
 * }
 * </pre>
 * <p>
 * Notice that at the top level we have the {@code given} field, it can be used to define variables accessible
 * in any step of  the usecase run. Each step also has the ability to define a {@code saved} field, which enables the
 * saving of parts of the response. </p>
 *
 * <p>
 * {@code given} meta variables are final, {@code saved} meta variables are updated in
 * the order of the usecase step execution and can be overwritten by upcoming steps.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Usecase {
    // The name of the file

    /**
     * <p>The name of the usecase file</p>
     * <p>
     * For the file {@code test/resources/usecase/test.json} provide the value
     * {@code test.json}
     * </p>
     */
    String value() default "";

    FileType type() default FileType.USECASE;

    /**
     * Defines the type of data used in a {@link Usecase}
     * and the directory in which to put files of this type.
     * <ul>
     *     <li>POSTMAN: {@code src/test/resources/postman/}</li>
     * </ul>
     */
    enum FileType {
        USECASE(Paths.get("src/test/resources/usecase"));

        public final Path pathPrefix;

        FileType(Path path) {
            pathPrefix = path;
        }
    }
}
