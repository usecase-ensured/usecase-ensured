package com.github.bitknot_project.progressive_testing.data

import com.fasterxml.jackson.databind.JsonNode
import java.nio.file.Path

data class TestStep(
    val filePath: Path,
    val name: String,
    val request: Request,
    val expectedResponse: ExpectedResponse?
) {
    fun asTraceHint(): String {
        return "STEP [$name] FILE [$filePath]"
    }

    fun asTraceHint(fieldName: String, json: JsonNode): String {
        val topHightlight =   "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
        val bottomHighlight = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"

        val firstLine = "STEP [$name] FIELD [$fieldName] FILE [$filePath]"
        val s = json.toPrettyString()
        val newS = mutableListOf<String>(firstLine)

        s.lines().onEachIndexed { index, line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("\"$fieldName\"")) {
                newS += line + "    <<<<<<< HERE IS THE PROBLEM"
            } else {
                newS += line
            }
        }
        return """${newS.joinToString(System.lineSeparator())}
        """.trimIndent()
    }

}
