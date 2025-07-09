package dummy_api

import com.github.bitknot_project.progressive_testing.ProgressiveTestingExtension
import com.github.bitknot_project.progressive_testing.TestFile
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(ProgressiveTestingExtension::class)
class IntegrationTest(
    @Autowired private val template: TestRestTemplate,
    @Autowired private val controller: DummyController,
) {
    private val url: String = "http://localhost:8080/dummy"

    @BeforeEach
    fun teardown() {
        controller.reset()
    }

    @Test
    fun `can create dummy`() {
        val req = DummyCreationRequest("a")

        val resp = template.postForEntity(url, req, DummyDto::class.java)

        assertEquals(201, resp.statusCode.value())
        assertEquals(DummyDto(0, "a"), resp.body!!)

        val x = 1
    }

    @Test
    fun `can retrieve dummy`() {
        val req = DummyCreationRequest("a")

        template.postForEntity(url, req, DummyDto::class.java)

        val resp = template.getForEntity<DummyDto>("$url/0")

        assertEquals(200, resp.statusCode.value())
        assertEquals(DummyDto(0, "a"), resp.body!!)
    }

    @Test
    fun `can list dummies`() {
        val req = DummyCreationRequest("a")
        val req2 = DummyCreationRequest("b")

        template.postForEntity(url, req, DummyDto::class.java)
        template.postForEntity(url, req2, DummyDto::class.java)

        val resp = template.getForEntity<Array<DummyDto>>("$url/all")

        assertEquals(200, resp.statusCode.value())
        assertArrayEquals(
            arrayOf(DummyDto(0, "a"), DummyDto(1, "b")), resp
                .body!!
        )
    }

    @Test
    fun `can call secret endpoint`() {
        val headers = HttpHeaders()
        headers.add("Authorization", "Basic Ym9iOmJvYg==")
        val response = template.exchange(
            "$url/secret", HttpMethod.GET,
            HttpEntity<Any>(headers),
            String::class.java
        )

        assertEquals(response.statusCode, HttpStatus.OK)
    }

    @Test
    @TestFile("a-test.json")
    fun `can create dummy ALT`() {

    }

    @Test
    @TestFile("secret.json")
    fun `can call secret endpoint (2)`(){}

    @Test
    @TestFile("multi-step.json")
    fun `can retrieve dummy (2)`() {}
}