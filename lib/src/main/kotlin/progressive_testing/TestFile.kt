package progressive_testing

import org.junit.jupiter.api.Test

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Test
annotation class TestFile(val value: String)
