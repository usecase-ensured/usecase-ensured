package com.github.usecase_assured;

import java.lang.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This annotation is to be used on a test method.
 * <b>PREREQUISITE</b>: your app is expected to actually be running on a local port, this is an
 * integration test!
 * Displaying the {@code run} button in the IDE near the test method signature requires the usual
 * {@link org.junit.jupiter.api.Test} annotation.
 * <h2>Supported formats</h2>
 * <h4>Postman</h4>
 * The Postman collections are expected in the {@code test/resources/postman} folder of your project.
 * Simply exporting collections from Postman is enough, they should be executed correctly.
 * <b>Postman environment variables are not supported</b> at the moment.
 * Postman's <b>post-response script</b> is used to <b>add assertions</b> to any step of the collection.
 * The content is expected to have a certain structure, here is an example:
 * <pre>
 * {@code
 * const response = {
 * "response": 200,
 * "content": {
 * "id": 0,
 * "name": "dummy"
 * "age": "{{any}}"
 * }
 * }
 * }
 * </pre>
 * <b>The meta variable {@code "{{any}}"}</b> can be used to express that a given field
 * is allowed to have any value.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Usecase {
    // The name of the file
    String value() default "";

    // FileType.POSTMAN by default. Details in FileType
    FileType type() default FileType.POSTMAN;

    /**
     * Defines the type of data used in a {@link Usecase}
     * and the directory in which to put files of this type.
     * <ul>
     *     <li>POSTMAN: {@code src/test/resources/postman/}</li>
     * </ul>
     */
    enum FileType {
        POSTMAN(Paths.get("src/test/resources/postman/"));

        final Path pathPrefix;

        FileType(Path path) {
            pathPrefix = path;
        }
    }
}
