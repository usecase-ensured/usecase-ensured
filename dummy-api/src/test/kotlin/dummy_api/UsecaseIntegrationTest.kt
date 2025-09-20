package dummy_api

import com.github.usecase_ensured.Usecase
import com.github.usecase_ensured.UsecaseEnsuredExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(UsecaseEnsuredExtension::class)
class UsecaseIntegrationTest(@Autowired private val controller: DummyController) {

    @BeforeEach
    fun teardown() {
        controller.reset()
    }

    @Test
    @Usecase("create-and-fetch.json")
    fun `create and fetch`() {
    }

    @Test
    @Usecase("secure.json")
    fun `access secure endpoint`() {
    }

    @Nested
    inner class SavedFeatureTestSuite {
        @Test
        @Usecase("save-feature/create-and-fetch.json")
        fun `create and fetch with saved variable assertion`() {
        }

        @Test
        @Usecase("save-feature/create-detailed.json")
        fun `use saved meta variables in nested JSON`() {
        }
    }

    @Nested
    inner class GivenFeatureTestSuite {
        @Test
        @Usecase(value = "given-feature/create-and-fetch.json")
        fun `define meta variables outside of test steps and use them throughout the usecase`() {
        }
    }

}