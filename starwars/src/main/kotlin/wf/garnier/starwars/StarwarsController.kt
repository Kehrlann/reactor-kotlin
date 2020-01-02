package wf.garnier.starwars

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("api")
class StarwarsController(private val people: Map<Int, Person>, private val films: Map<Int, Film>) {

    @GetMapping("/people/")
    fun getPeople(): Flux<Person> {
        return Flux.fromIterable(people.values)
    }

    @GetMapping("/people/{id}/")
    fun getPerson(@PathVariable id: Int): Mono<ResponseEntity<Person>> {
        return Mono.justOrEmpty(people[id])
                .map { ResponseEntity.ok().body(it) }
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/films/{id}/")
    fun getFilm(@PathVariable id: Int): Mono<ResponseEntity<Film>> {
        return Mono.justOrEmpty(films[id])
                .map { ResponseEntity.ok().body(it) }
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }
}