package wf.garnier.reactorfun

import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.netty.http.client.HttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.system.measureTimeMillis


private val client = HttpClient.create().baseUrl("http://localhost:8080/")
const val lukeUrl = "/api/people/1/"

fun main() {
    println("Listing Characters in Movies !")

    val totalDuration = measureTimeMillis {
        val films = getPerson(lukeUrl)
                .flatMapIterable { it.films }
                .flatMap { getFilm(it) }
                .flatMap { getCharactersForFilm(it) }
                .toIterable()
                .joinToString("\n\n\n")

        println(films)
    }

    println()
    println("Finished in ${totalDuration}ms")
}

fun getCharactersForFilm(baseFilm: Film): Mono<Film> {
    return Flux.fromIterable(baseFilm.characters)
            .flatMap {
                getPerson(it)
            }
            .map { it.name }
            .collectList()
            .map { Film(baseFilm.title, baseFilm.url, it) }
}


val cache: ConcurrentMap<String, in Signal<out Person>> = ConcurrentHashMap()
private fun getPerson(url: String): Mono<Person> {
    return CacheMono.lookup(cache, url)
            .onCacheMissResume(
                    client.get()
                            .uri(url)
                            .responseContent()
                            .aggregate()
                            .asString()
                            .map { unmarshalPerson(it) }
            )
}

private fun getFilm(url: String): Mono<Film> {
    return client.get()
            .uri(url)
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue(it, Film::class.java) }
}


val simpleCache: ConcurrentMap<String, Mono<Person>> = ConcurrentHashMap()
private fun getPerson2(url: String): Mono<Person> {
    return simpleCache.getOrPut(url, {
        client.get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .map { unmarshalPerson(it) }
                .cache()
    })
}

private fun unmarshalPerson(it: String?) = mapper.readValue<Person>(it, Person::class.java)