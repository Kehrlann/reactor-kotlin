import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.netty.http.client.HttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.system.measureTimeMillis

const val lukeUrl = "https://swapi.co/api/people/1/"

fun main() {
    println("Listing Characters in Movies !")
    var films: String = ""
    val time = measureTimeMillis {
        films = getPerson(lukeUrl)
                .flatMapIterable { it.films }
                .flatMap { getFilm(it) }
                .flatMap { getCharactersForFilm(it) }
                .map { it.toString() }
                .toIterable()
                .joinToString("\n\n\n")

    }

    println(films)
    println()
    println("Got everything in ${time}ms")
}

fun getCharactersForFilm(baseFilm: Film): Mono<Film> {
    return Flux.fromIterable(baseFilm.characters)
            .flatMap {
                getPerson(it)
                        .doOnNext { p -> println("Got ${p.name} for film ${baseFilm.title}") }
            }
            .map { it.name }
            .collectList()
            .map { Film(baseFilm.title, it) }
}


val cache: ConcurrentMap<String, in Signal<out Mono<Person>>> = ConcurrentHashMap()
private fun getPerson(url: String): Mono<Person> {
    return CacheMono.lookup(cache, url)
            .onCacheMissResume(
                    Mono.just(client.get()
                            .uri(url)
                            .responseContent()
                            .aggregate()
                            .asString()
                            .map { mapper.readValue<Person>(it, Person::class.java) }
                            .doOnNext { p -> println("REQUESTED ${p.name}") }
                            .cache()
                    )
            )
            .flatMap { it }
}

private fun getFilm(url: String): Mono<Film> {
    return client.get()
            .uri(url)
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue(it, Film::class.java) }
            .doOnNext { println("Got film: ${it.title}") }
}


val simpleCache: ConcurrentMap<String, Mono<Person>> = ConcurrentHashMap();
private fun getPerson2(url: String): Mono<Person> {
    return simpleCache.getOrPut(url, {
        client.get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .map { mapper.readValue<Person>(it, Person::class.java) }
                .cache()
    })
}
