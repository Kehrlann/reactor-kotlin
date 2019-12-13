data class PaginatedResource<T>(val count: Int, val next: String?, val previous: String?, val results: List<T>)
