import com.fasterxml.jackson.module.kotlin.readValue
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun main(args: Array<String>) {
    println("Listing all characters ...")
    println("")

    val result = getPage(1)
            .expand { it ->
                if (it.isFirst) {
                    // If it is the first page, load all the other ones
                    val numberOfPages = it.count / it.results.count()
                    Flux.range(2, numberOfPages)
                            .flatMap {
                                getPage(it)
                            }
                } else {
                    // If it is a subsequent page, don't load anything
                    Flux.empty()
                }
            }
            .map { it.results }
            .flatMapIterable { it }
            .collectList()
            .block()

    println("Total characters: ${result?.size ?: 0}")
    println("")

    println(result
            ?.map { "- $it" }
            ?.joinToString("\n")
    )
}

private fun getPage(pageNumber: Int): Mono<PaginatedResource<Person>> {
    return client.get()
            .uri("/people/?page=$pageNumber")
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue<PaginatedResource<Person>>(it) }
}