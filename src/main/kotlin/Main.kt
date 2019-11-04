import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient

val baseUrl = "https://swapi.co/api"
val client = HttpClient.create()
        .baseUrl(baseUrl)

fun main() {
    val mapper = jacksonObjectMapper()
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
            .flatMap {
                client.get()
                        .uri(it)
                        .responseContent()
                        .aggregate()
                        .asString()
            }
            .map { mapper.readValue(it, Film::class.java).title }
            .toIterable()
            .joinToString(",\n")

    println(films)
}

@JsonIgnoreProperties(ignoreUnknown = true)
class PeopleResponse(val results: List<Person>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(val name: String, val films: List<String>)

@JsonIgnoreProperties(ignoreUnknown = true)
class Film(val title: String, val characters: List<String>)