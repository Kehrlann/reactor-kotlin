package wf.garnier.starwars

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
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
        val delay = Duration.ofMillis(50L + 100L * currentRequests.get())
        println("Preparing people request for ${id}. Delaying by ... ${delay.toMillis()}ms")
        return Mono.justOrEmpty(people[id])
                .map { ResponseEntity.ok().body(it) }
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .delayElement(delay)
                .doOnSubscribe {
                    val numberOfRequests = currentRequests.incrementAndGet()
                    println("Subscribed to people request for $id. $numberOfRequests in flight")
                }
                .doOnTerminate { currentRequests.decrementAndGet() }
    }

    @GetMapping("/films/{id}/")
    fun getFilm(@PathVariable id: Int): Mono<ResponseEntity<Film>> {
        return Mono.justOrEmpty(films[id])
                .map { ResponseEntity.ok().body(it) }
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .delayElement(Duration.ofMillis(50L + 100L * currentRequests.get()))
                .doOnSubscribe { currentRequests.incrementAndGet() }
                .doOnTerminate { currentRequests.decrementAndGet() }
    }
}