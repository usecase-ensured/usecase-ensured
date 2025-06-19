package progressive_testing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.nio.file.Files
import java.nio.file.Paths

class ProgressiveTestingExtension : BeforeTestExecutionCallback {
    override fun beforeTestExecution(context: ExtensionContext?) {
        val maybeMethod = context!!.testMethod

        if (maybeMethod.isPresent) {
            val method = maybeMethod.get()

            val testingAnnotation: TestFile? = method.getAnnotation(TestFile::class.java)

            if (testingAnnotation != null) {
                val file = testingAnnotation.value
                if (Files.isDirectory(Paths.get(file))) {
                    Assertions.assertFalse(true)
                }
            }
        }
    }
}