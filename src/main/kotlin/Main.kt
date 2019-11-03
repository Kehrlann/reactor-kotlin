import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.netty.http.client.HttpClient

val baseUrl = "https://swapi.co/api"

fun main() {
    val mapper = jacksonObjectMapper()
    println("Hello world !")
    val names = HttpClient.create()
            .baseUrl(baseUrl)
            .get()
            .uri("/people/")
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue(it, PeopleResponse::class.java) }
            .map { it.results }
            .block()!!
            .map { it.name }

    println(names.joinToString(",\n"))
}

@JsonIgnoreProperties(ignoreUnknown = true)
class PeopleResponse(val results: List<Person>) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(val name: String) {}