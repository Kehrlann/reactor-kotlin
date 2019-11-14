import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

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
//                        .doOnNext { p -> println("Got ${p.name} for film ${baseFilm.title}") }
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
