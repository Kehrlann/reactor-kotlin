import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.netty.http.client.HttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


val client = HttpClient.create()
val mapper = jacksonObjectMapper()

val cache: ConcurrentMap<String, in Signal<out Person>> = ConcurrentHashMap();

fun main() {
    println("Hello world !")
    val lukeUrl = "https://swapi.co/api/people/1/"
    val luke: Mono<Person> = CacheMono.lookup(cache, lukeUrl)
            .onCacheMissResume(client
                    .get()
                    .uri(lukeUrl)
                    .responseContent()
                    .aggregate()
                    .asString()
                    .map { mapper.readValue<Person>(it, Person::class.java) }
            )

    val films = luke
            .doOnNext { println(it) }
            .flatMapIterable { it.films }
            .flatMap { getFilm(it) }
            .flatMap { getCharactersForFilm(it) }
            .map { it.toString() }
            .toIterable()
            .joinToString("\n\n\n")

    println(films)
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
            .reduce(listOf<String>(), { list, person -> list.plus(person.name) })
            .map { Film(baseFilm.title, it) }
}

private fun getPerson(url: String): Mono<Person> {
    return CacheMono.lookup(cache, url)
            .onCacheMissResume(
                    client.get()
                            .uri(url)
                            .responseContent()
                            .aggregate()
                            .asString()
                            .map { mapper.readValue<Person>(it, Person::class.java) }
                            .doOnNext { p -> println("Got ${p.name} from request") }
            )
}


private fun getFilm(url: String): Mono<Film> {
    return client.get()
            .uri(url)
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue(it, Film::class.java) }
//            .doOnNext { println("Got film: ${it.title}") }
}
