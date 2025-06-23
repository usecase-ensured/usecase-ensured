package progressive_testing

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import progressive_testing.data.ExpectedResponse
import progressive_testing.data.Request
import progressive_testing.data.TestStep
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ProgressiveTestingExtension : BeforeTestExecutionCallback {
    private companion object {
        val PREFIX = Paths.get("src/test/resources/postman/")
        val MAPPER = ObjectMapper()
    }

    override fun beforeTestExecution(context: ExtensionContext?) {
        val maybeMethod = context!!.testMethod

        if (maybeMethod.isPresent) {
            val method = maybeMethod.get()

            val annotation = method.getAnnotation(TestFile::class.java)
            val testFileAnnotation = annotation ?: return

            val filePath = PREFIX.resolve(testFileAnnotation.value)

            val steps = buildSteps(filePath)

            for (step in steps) {
                val response = doRequest(step)

                if (step.expectedResponse != null) {
                    assertEquals(step.expectedResponse, response)
                }

            }
        }
    }

    private fun ensureThatPathPointsToFile(path: Path) {

        if (Files.notExists(path)) {
            throw RuntimeException("$path is not a valid file")
        }

        if (!Files.isRegularFile(path)) {
            throw RuntimeException(
                "$path must be an actual, simple " +
                        "file"
            )
        }
    }

    private fun buildSteps(path: Path): List<TestStep> {
        ensureThatPathPointsToFile(path)

        val maybeJson: JsonNode?
        try {
            maybeJson =
                MAPPER.reader().readTree(FileInputStream(path.toFile()))
        } catch (e: IOException) {
            throw RuntimeException(
                "failed to parse file '$path'. invalid " +
                        "JSON", e
            )
        }

        val json = maybeJson!!

        val entries = json.get("item")
        assert(entries.isArray, { "this is supposed to be a JSON array" })

        val steps = mutableListOf<TestStep>()

        for (entry in entries) {
            val name = entry.requiredAt("/name").asText()
            val method = entry.requiredAt("/request/method").asText() // used
            val header = entry.requiredAt("/request/header").asText() // used
            // this method for
            val url = entry.requiredAt("/request/url/raw").asText()

            val request = Request(method, header, url)
            val expectation = entry.requiredAt("/event/0/script/exec")
                .valueStream().map { it -> it.asText() }
                .reduce { acc, s -> acc + s }.get().toString()
                .replace(Regex(" +"), " ")

            val jsonValue = expectation.split("=").get(1).trim()
            val parsedExpectedJson = ExpectedResponse(MAPPER.reader().readTree(jsonValue))

            val step = TestStep(name, request, parsedExpectedJson)

            steps += step
        }

        return steps.toList()

    }
}