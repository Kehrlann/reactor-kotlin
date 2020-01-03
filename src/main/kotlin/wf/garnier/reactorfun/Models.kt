package wf.garnier.reactorfun

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(val name: String, val url: String, val films: List<String>) {
    override fun toString() =
            "{ \"name\": \"$name\", \"url\": \"${url.asResource()}\", \"films\": ${films.asResources()} }"
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Film(val title: String, val url: String, val characters: List<String>) {
    override fun toString(): String {
        val title = "# $title\n---------\n- "
        return title + characters.joinToString("\n- ")
    }

    fun toJson(): String {
        return "{ \"title\": \"$title\", \"url\": \"${url.asResource()}\", \"characters\": ${characters.asResources()} }"
    }
}

private fun String.asResource() = this.removePrefix("https://swapi.co")
private fun List<String>.asResources() = this.map { '"' + it.asResource() + '"' }