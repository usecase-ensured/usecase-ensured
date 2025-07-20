package com.github.usecase_assured

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.usecase_assured.data.ExpectedResponse
import com.github.usecase_assured.data.Request
import com.github.usecase_assured.data.TestStep
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.http.Headers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Enables the functionality of running test cases based on external files,
 * for example Postman collections. Configured per test method with the
 * [com.github.bitknot_project.progressive_testing.TestFile] annotation.
 *
 * Apply on the test class with the [org.junit.jupiter.api.extension.ExtendWith]
 * annotation.
 */
class ProgressiveTestingExtension : BeforeTestExecutionCallback {
    private companion object {
        val MAPPER = ObjectMapper()
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        val maybeMethod = context.testMethod

        if (maybeMethod.isPresent) {
            val method = maybeMethod.get()

            val annotation = method.getAnnotation(TestFile::class.java) ?: return

            val filePath = annotation.type.pathPrefix.resolve(annotation.value)

            val steps = buildSteps(filePath)

            for (step in steps) {
                doRequest(step)
            }
        }
    }

    private fun doRequest(step: TestStep) {
        val builder = given()

        builder.baseUri(step.request.url)

        step.request.body?.let { builder.body(it) }

        builder.headers(Headers(step.request.headers))
        builder.contentType(ContentType.JSON)
        builder.accept(ContentType.JSON)

        val response = builder.request(step.request.method.name)
        step.expectedResponse?.let {
            val expectedStatusCode = it.expectedResponse.at("/response")
            if (!expectedStatusCode.isMissingNode) {
                val msg = "invalid HTTP status: " + step.asTraceHint()
                assertEquals(expectedStatusCode.asInt(), response.statusCode, msg)
                response.then().statusCode(expectedStatusCode.asInt())
            }

            val expected = it.expectedResponse.at("/content")!!
            if (!expected.isMissingNode) {
                val actual =
                    response.body.`as`(JsonNode::class.java) as JsonNode

                val fieldAssertions = createFieldAssertions(expected, actual, step)
                fieldAssertions.forEach { assertion -> assertion.invoke() }
            }
        }
    }

    private fun createFieldAssertions(
        expectedNode: JsonNode,
        actualNode: JsonNode,
        step: TestStep
    ): List<() -> Unit> {
        val acc = mutableListOf<() -> Unit>()
        val actualValueMap = mutableMapOf<String, JsonNode>()
        actualNode.forEachEntry { k, v -> actualValueMap[k] = v }

        val expectedValueMap = mutableMapOf<String, JsonNode>()
        expectedNode.forEachEntry { k, v -> expectedValueMap[k] = v }

        val unexpectedFields = expectedValueMap.keys.minus(actualValueMap.keys).toMutableList()
        unexpectedFields += actualValueMap.keys.minus(expectedValueMap.keys)

        if (unexpectedFields.isNotEmpty()) {
            return listOf {
                fail("${step.asTraceHint()}: unexpected fields $unexpectedFields present")
            }
        }
        actualNode.forEachEntry { fieldName, actual ->
            val expected = expectedNode.at("/$fieldName")
            val metaAssertion = metaVariableAssertion(expected, actualNode, fieldName, step)
            if (metaAssertion == null) {
                assertEquals(expected, actual, step.asTraceHint(fieldName, actualNode))
            } else {
                acc += metaAssertion
            }
        }
        return acc.toList()
    }

    private fun metaVariableAssertion(
        expected: JsonNode,
        actualJson: JsonNode,
        actualFieldName: String,
        step: TestStep
    ): (() -> Unit)? {
        if (expected.isTextual && expected.asText() == "{{any}}") {
            return { assertTrue(true, step.asTraceHint(actualFieldName, actualJson)) }
        }
        return null
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
            val headersNode = entry.requiredAt("/request/header")

            val headers = headersNode.associate {
                it.requiredAt("/key").asText() to it.requiredAt("/value")
                    .asText()
            }.map { (k, v) -> Header(k, v) }

            val url = entry.requiredAt("/request/url/raw").asText()
            val body =
                entry.at("/request/body/raw")
                    .takeUnless { it.isMissingNode }
                    ?.asText()

            val request =
                Request(Request.Method.valueOf(method), headers, url, body)
            val expectation = entry.requiredAt("/event/0/script/exec")
                .valueStream().map { it.asText() }
                .reduce { acc, s -> acc + s }.get()
                .replace(Regex(" +"), " ")

            val jsonValue = expectation.split("=").get(1).trim()
            val parsedExpectedJson =
                ExpectedResponse(MAPPER.reader().readTree(jsonValue))

            val step = TestStep(path, name, request, parsedExpectedJson)

            steps += step
        }

        return steps.toList()

    }
}