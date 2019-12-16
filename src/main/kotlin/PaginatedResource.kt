import kotlin.math.ceil

class PaginatedResource<T>(val count: Int, val next: String?, val previous: String?, val results: List<T>) {
    val isFirst = previous == null
    val numberOfPages = ceil(count.toDouble() / results.count()).toInt()
}
