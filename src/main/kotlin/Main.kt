import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.netty.http.client.HttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime


val client = HttpClient.create()
val mapper = jacksonObjectMapper()
val lukeUrl = "https://swapi.co/api/people/1/"

val cache: ConcurrentMap<String, in Signal<out Mono<Person>>> = ConcurrentHashMap();

fun main() {
    println("Hello world !")
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(val name: String, val films: List<String>)

@JsonIgnoreProperties(ignoreUnknown = true)
class Film(val title: String, val characters: List<String>) {
    override fun toString(): String {
        val title = "# $title\n---------\n- "
        return title + characters.joinToString("\n- ")
    }
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
