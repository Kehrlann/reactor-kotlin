import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(val name: String, val films: List<String>)

@JsonIgnoreProperties(ignoreUnknown = true)
class Film(val title: String, val characters: List<String>) {
    override fun toString(): String {
        val title = "# $title\n---------\n- "
        return title + characters.joinToString("\n- ")
    }
}