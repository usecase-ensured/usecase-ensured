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
    fun `can create dummy`() {}

    @Test
    @Usecase("a.json", type = Usecase.FileType.USECASE)
    fun `can create dummy (2)`() {}

    @Test
    @Usecase("secured.json")
    fun `can call secured endpoint`(){}

    @Test
    @Usecase("multi-step.json")
    fun `can retrieve dummy`() {}

    @Test
    @Usecase("meta-variable.json")
    fun `can use meta variables`() {}
}