package wf.garnier.starwars


class Person(val name: String, val films: List<String>, url: String): Resource(url)

class Film(val title: String, val characters: List<String>, url: String): Resource(url)

open class Resource(private val url: String) {
    val id: Int
        get() = Regex("/api/(?<resourceType>.*)/(?<id>\\d+)/").find(url)!!.groups["id"]!!.value.toInt()
}