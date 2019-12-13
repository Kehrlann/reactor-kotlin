import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.netty.http.client.HttpClient

val client = HttpClient.create().baseUrl("https://swapi.co/api")
val mapper = jacksonObjectMapper()