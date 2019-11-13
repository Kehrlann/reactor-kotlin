import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.context.Context
import java.util.*

val baseUrl = "https://swapi.co/api"
val client = HttpClient.create()
        .baseUrl(baseUrl)
val mapper = jacksonObjectMapper()

fun main() {
    println("Hello world !")
    val films = client
            .get()
            .uri("/people/?search=luke")
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue(it, PeopleResponse::class.java) }
            .flatMapIterable { it.results }
            .doOnNext { person ->
                println("ON next $person")
                Mono.subscriberContext()
                        .doOnNext{ ctx -> println("got context !") }
                        .map { ctx -> ctx.put(person.url, Mono.just(person)) }
                        .doOnNext { ctx -> println("put ${person.url} in cache, ${ctx.hasKey(person.url)}") }
            }
            .flatMapIterable { it.films }
            .flatMap { getFilm(it) }
            .flatMap { getCharactersForFilm(it) }
            .map { it.toString() }
            .toIterable()
            .joinToString("\n\n\n")

    println(films)
}

@JsonIgnoreProperties(ignoreUnknown = true)
class PeopleResponse(val results: List<Person>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(val name: String, val films: List<String>, val url: String)

@JsonIgnoreProperties(ignoreUnknown = true)
class Film(val title: String, val characters: List<String>) {

    override fun toString(): String {
        val title = "# $title\n---------\n- "
        return title + characters.joinToString("\n- ")
    }
}

fun getCharactersForFilm(baseFilm: Film): Mono<Film> {
    return Flux.fromIterable(baseFilm.characters)
            .flatMap { url ->
                println("Getting $url")
                Mono.subscriberContext()
                        .map { ctx ->
                            ctx.getOrEmpty<Mono<Person>>(url)
                                    .map { mp ->
                                        println("got $url from cache")
                                        mp
                                    }
                                    .orElseGet {
                                        println("${System.currentTimeMillis()} new request for $url")
                                        val newReq = getPerson(url)
                                        ctx.put(url, newReq)
                                        newReq
                                    }
                        }
                        .flatMap { it }
            }
            .reduce(listOf<String>(), { list, person -> list.plus(person.name) })
            .map { Film(baseFilm.title, it) }
}

private fun getPerson(url: String): Mono<Person> {
    return client.get()
            .uri(url)
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue<Person>(it, Person::class.java) }
            .cache()
}


private fun getFilm(url: String): Mono<Film> {
    return client.get()
            .uri(url)
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue(it, Film::class.java) }
}
