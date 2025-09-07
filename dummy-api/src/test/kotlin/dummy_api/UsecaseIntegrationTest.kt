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
    @Usecase("create-and-fetch.json", type = Usecase.FileType.USECASE)
    fun `can create and fetch`() {
    }

    @Test
    @Usecase("secure.json", type = Usecase.FileType.USECASE)
    fun `can access secure endpoint`() {
    }

    @Nested
    inner class SavedFeatureTestSuite {
        @Test
        @Usecase("save-feature/create-and-fetch.json", type = Usecase.FileType.USECASE)
        fun `can create and fetch with saved variable assertion`() {
        }
    }

}