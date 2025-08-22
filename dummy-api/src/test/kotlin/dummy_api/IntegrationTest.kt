package dummy_api

import com.github.usecase_ensured.Usecase
import com.github.usecase_ensured.UsecaseEnsuredExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(UsecaseEnsuredExtension::class)
class IntegrationTest(@Autowired private val controller: DummyController, ) {

    @BeforeEach
    fun teardown() {
        controller.reset()
    }

    @Test
    @Usecase("a-test.json")
    fun `can create dummy ALT`() {}

    @Test
    @Usecase("secret.json")
    fun `can call secret endpoint (2)`(){}

    @Test
    @Usecase("multi-step.json")
    fun `can retrieve dummy (2)`() {}

    @Test
    @Usecase("meta-variable.json")
    fun `can use meta variables`() {}
}