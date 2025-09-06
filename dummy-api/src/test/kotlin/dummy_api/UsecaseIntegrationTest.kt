package dummy_api

import com.github.usecase_ensured.Usecase
import com.github.usecase_ensured.UsecaseEnsuredExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(UsecaseEnsuredExtension::class)
class UsecaseIntegrationTest {
    @Test
    @Usecase("create-and-fetch.json", type = Usecase.FileType.USECASE)
    fun `can create and fetch`() {
    }

    @Test
    @Usecase("secure.json", type = Usecase.FileType.USECASE)
    fun `can access secure endpoint`() {
    }

}