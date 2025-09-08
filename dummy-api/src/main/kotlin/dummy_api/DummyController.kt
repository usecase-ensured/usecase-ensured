package dummy_api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("dummy")
class DummyController {

    var idCounter = 0;

    val store = mutableMapOf<Int, DummyDto>()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody dto: DummyCreationRequest) : DummyDto {
        val createdDummy = DummyDto(idCounter++, dto.name)

        store.put(createdDummy.id, createdDummy)

        return createdDummy
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: Int) : DummyDto {
        return store.get(id) ?: throw RuntimeException("dummy $id not found")
    }

    @GetMapping("secret")
    fun getSecret() : String {
        return "\"this is a secret\""
    }

    @GetMapping("all")
    fun list() : List<DummyDto> {
        return store.values.toList()
    }

    fun reset() {
        store.clear()
        idCounter = 0
    }

    data class DummyDto(val id: Int, val name: String)
    data class DummyCreationRequest(val name: String)
}