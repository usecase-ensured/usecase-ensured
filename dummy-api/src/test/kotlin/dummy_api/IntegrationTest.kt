package dummy_api

import com.github.usecase_ensured.Usecase
import com.github.usecase_ensured.UsecaseEnsuredExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(UsecaseEnsuredExtension::class)
class IntegrationTest(@Autowired private val controller: DummyController) {

    @BeforeEach
    fun teardown() {
        controller.reset()
    }

    @Nested
    inner class PostmanTestSuite {
        @Test
        @Usecase("create.json")
        fun `can create dummy`() {
        }

        @Test
        @Usecase("secured.json")
        fun `can call secured endpoint`() {
        }

        @Test
        @Usecase("multi-step.json")
        fun `can retrieve dummy`() {
        }
    }

    @Nested
    inner class UsecaseTestSuite {

        @Test
        @Usecase("create.json", type = Usecase.FileType.USECASE)
        fun `can create dummy`() {
        }

        @Test
        @Usecase("secured.json", type = Usecase.FileType.USECASE)
        fun `can call secured endpoint`() {
        }

        @Test
        @Usecase("multi-step.json", type = Usecase.FileType.USECASE)
        fun `can retrieve dummy`() {
        }

        @Disabled
        @Test
        @Usecase("remembered/create-then-fetch.json", type = Usecase.FileType.USECASE)
        fun `can create and then assert on retrieve with meta variable`() {}

    }
}