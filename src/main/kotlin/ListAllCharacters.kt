import com.fasterxml.jackson.module.kotlin.readValue
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun main(args: Array<String>) {
    println("Listing all characters ...")

    val result = getPage(1)
            .flatMapMany {
                val numberOfPages = it.count / it.results.count()
                Flux.range(2, numberOfPages)
                        .flatMap {
                            getPage(it)
                        }
                        .map { it.results }
            }
            .flatMapIterable { it }
            .collectList()
            .block()
    println(result?.size ?: 0)
    println(result)
}

private fun getPage(pageNumber: Int): Mono<PaginatedResource<Person>> {
    return client.get()
            .uri("/people/?page=$pageNumber")
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue<PaginatedResource<Person>>(it) }
}