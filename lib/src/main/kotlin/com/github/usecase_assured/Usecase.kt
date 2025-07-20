package com.github.usecase_assured

import java.lang.annotation.Inherited
import java.nio.file.Path
import java.nio.file.Paths

/**
 * This annotation is to be used on a test method.
 *
 * **PREREQUISITE**: your app is expected to actually be running on a local port, this is an
 * integration test!
 *
 * Displaying the `run` button in the IDE near the test method signature requires the usual
 * [org.junit.jupiter.api.Test] annotation.
 *
 * ## Supported formats
 * #### Postman
 * The Postman collection files are expected in the `test/resources/postman`
 * folder. Simply exporting collections from Postman is enough, they should
 * be executed correctly.
 *
 * Postman's **post-response script** is used to **add assertions** to any step of the collection.
 * The content is expected to have a certain structure, here is an example:
 *
 * ```
 * const response = {
 *     "response": 200,
 *     "content": {
 *         "id": 0,
 *         "name": "dummy"
 *         "age": "{{any}}"
 *     }
 * }
 * ```
 *
 * **The meta variable `"{{any}}"`** can be used to express that a given field
 * is allowed to have any value.
 *
 * @param value The name of the file
 * @param type [FileType.POSTMAN] by default. Details in [FileType]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Usecase(
    val value: String,
    val type: FileType = FileType.POSTMAN
)

/**
 * Defines the type of data used in a [Usecase]
 * and the directory in which to put files of this type.
 *
 * - POSTMAN: `src/test/resources/postman/`
 */
enum class FileType(val pathPrefix: Path) {
    POSTMAN(Paths.get("src/test/resources/postman/"));
}
