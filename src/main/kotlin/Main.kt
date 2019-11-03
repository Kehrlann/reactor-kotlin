import reactor.netty.http.client.HttpClient

val baseUrl = "https://swapi.co/api"

fun main() {
    println("Hello world !")
    val first = HttpClient.create()
            .baseUrl(baseUrl)
            .get()
            .uri("/people/")
            .responseContent()
            .asString()
            .blockFirst()

    println(first)
}