package dummy_api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * controller for demonstrating nested meta variables
 */
@RestController
@RequestMapping("dummy/detailed")
class DetailedDummyController {

    var idCounter = 0;

    val store = mutableMapOf<Int, DetailedDummyDto>()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createdDetailedDummy(@RequestBody request: DetailedDummyCreationRequest): DetailedDummyDto {
        val detailedDummy = DetailedDummyDto(request.name, idCounter++, request.details)
        store[detailedDummy.id] = detailedDummy

        return detailedDummy
    }

    @GetMapping("{id}")
    fun getDetailedDummy(@PathVariable id: Int): DetailedDummyDto {
        return store[id] ?: throw RuntimeException("dummy $id not found")
    }

    data class DetailedDummyDto(val name: String, val id: Int, val details: DetailsDto)
    data class DetailedDummyCreationRequest(val name: String, val details: DetailsDto)

    data class DetailsDto(val age: Int, val favoriteColor: String)
}