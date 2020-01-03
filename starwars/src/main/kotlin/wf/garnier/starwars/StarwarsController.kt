package wf.garnier.starwars

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicInteger

@RestController
@RequestMapping("api")
class StarwarsController(private val people: Map<Int, Person>, private val films: Map<Int, Film>) {

    private val currentRequests: AtomicInteger = AtomicInteger(0)

    @GetMapping("/people/")
    fun getPeople(): Flux<Person> {
        return Flux.fromIterable(people.values)
                .doOnSubscribe { currentRequests.incrementAndGet() }
                .doOnTerminate { currentRequests.decrementAndGet() }
    }

    @GetMapping("/people/{id}/")
    fun getPerson(@PathVariable id: Int): Mono<ResponseEntity<Person>> {
        return Mono.justOrEmpty(people[id])
                .map { ResponseEntity.ok().body(it) }
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnSubscribe { currentRequests.incrementAndGet() }
                .doOnTerminate { currentRequests.decrementAndGet() }
    }

    @GetMapping("/films/{id}/")
    fun getFilm(@PathVariable id: Int): Mono<ResponseEntity<Film>> {
        return Mono.justOrEmpty(films[id])
                .map { ResponseEntity.ok().body(it) }
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnSubscribe { currentRequests.incrementAndGet() }
                .doOnTerminate { currentRequests.decrementAndGet() }
    }
}