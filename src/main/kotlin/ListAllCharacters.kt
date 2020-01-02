import com.fasterxml.jackson.module.kotlin.readValue
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

private val client = HttpClient.create().baseUrl("https://swapi.co/")

fun main(args: Array<String>) {
    println("Listing all characters ...")
    println("")

    val result = getAllPages()
            .map { it.results }
            .flatMapIterable { it }
            .collectList()
            .block()

    println("")
    println("Total characters: ${result?.size ?: 0}")
    println("")

    println(result?.joinToString(",\n"))
}

private fun getPage(pageNumber: Int): Mono<PaginatedResource<Person>> {
    return client.get()
            .uri("/api/people/?page=$pageNumber")
            .responseContent()
            .aggregate()
            .asString()
            .map { mapper.readValue<PaginatedResource<Person>>(it) }
            .doOnSubscribe { println("Requesting page #$pageNumber ...") }
}

private fun getPageRange(start: Int, count: Int) =
        Flux.range(start, count).flatMap { getPage(it) }

// This function gets the first page, then from that, derives the total number of pages, then gets them all
// Note: it downloads the first page twice.
private fun getAllPagesNaive(): Flux<PaginatedResource<Person>> =
        getPage(1)
                .map { it.numberOfPages }
                .flatMapMany { Flux.range(1, it) }
                .flatMap { getPage(it) }

// This functions gets the first page and then the subsequent pages, using expand.
// However, it requires modifying the "paginatedResponse" to have a flag "isFirst"
private fun getAllPagesNoDuplication(): Flux<PaginatedResource<Person>> =
        getPage(1)
                .expand { it ->
                    if (it.isFirst) {
                        // If it is the first page, load the reste of the pages
                        Flux.range(2, it.numberOfPages - 1)
                                .flatMap { getPage(it) }
                    } else {
                        // If it is a subsequent page, don't load anything
                        Flux.empty()
                    }
                }


// This one gets all pages without needing to add "isFirst" in the paginated response
// and is the most explicit about what it does
private fun getAllPages() =
        getPage(1)
                .flatMapMany {
                    Mono
                            .just(it)
                            .concatWith(getPageRange(2, it.numberOfPages - 1))
                }

