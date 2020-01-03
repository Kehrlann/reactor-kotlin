package wf.garnier.reactorfun

import reactor.blockhound.BlockHound
import reactor.blockhound.integration.BlockHoundIntegration
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.core.scheduler.ReactorBlockHoundIntegration
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.NettyBlockHoundIntegration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


//private val wf.garnier.reactorfun.getClient = HttpClient.create().baseUrl("https://swapi.co/")
private val client = HttpClient.create().baseUrl("http://localhost:8080/")
const val lukeUrl = "/api/people/1/"

fun main() {
    BlockHound.builder()
            .with(ReactorBlockHoundIntegration())
            .with(NettyBlockHoundIntegration())
            .with(CustomIntegration())
            .install()
    println("Listing Characters in Movies !")

    val films = getPerson(lukeUrl)
            .flatMapIterable { it.films }
            .flatMap { getFilm(it) }
            .flatMap { getCharactersForFilm(it) }
            .toIterable()
            .joinToString("\n\n\n")

    println(films)
}

class CustomIntegration : BlockHoundIntegration {
    override fun applyTo(builder: BlockHound.Builder) {
        builder
                .allowBlockingCallsInside("sun.security.ssl.Handshaker", "kickstart") // netty SSL stuff
                .allowBlockingCallsInside("sun.security.ssl.Handshaker", "processLoop") // netty SSL stuff
                .allowBlockingCallsInside("com.fasterxml.jackson.databind.util.ClassUtil", "getPackageName")    // jackson
                .allowBlockingCallsInside("java.io.PrintStream", "println") // kotlin's println for doOnNext & stuff
                .allowBlockingCallsInside("CharactersWhoPlayedWithLukeKt", "wf.garnier.reactorfun.unmarshalPerson") // weird unmarshalling errors
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
                            .doOnNext { p -> println("REQUESTED ${p.name}") }
            )
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
                .map { unmarshalPerson(it) }
                .doOnNext { p -> println("REQUESTED ${p.name}") }
                .cache()
    })
}

private fun unmarshalPerson(it: String?) = mapper.readValue<Person>(it, Person::class.java)