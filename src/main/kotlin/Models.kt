import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(val name: String, val films: List<String>) {
    override fun toString() =
            "{ \"name\": \"$name\", \"films\": ${films.map { '"' + it.removePrefix("https://swapi.co") + '"' }} }"
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Film(val title: String, val characters: List<String>) {
    override fun toString(): String {
        val title = "# $title\n---------\n- "
        return title + characters.joinToString("\n- ")
    }

    fun toJson(): String {
        return "{ \"title\": \"$title\", \"characters\": ${characters.map { '"' + it.removePrefix("https://swapi.co") + '"' }} }"
    }
}