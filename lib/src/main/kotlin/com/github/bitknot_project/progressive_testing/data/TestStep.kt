package com.github.bitknot_project.progressive_testing.data

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

    fun asTraceHint(fieldName: String): String {
        return "STEP [$name] FIELD [$fieldName] FILE [$filePath]"
    }

}
