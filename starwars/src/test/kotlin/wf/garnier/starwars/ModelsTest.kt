package wf.garnier.starwars

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ModelsTest {
    @Test
    fun getsId() {
        val person = Person("someone", emptyList(), "/api/people/1/")
        val film = Film("some film", emptyList(), "/api/films/12/")

        assertThat(person.resourceId()).isEqualTo(1)
        assertThat(film.resourceId()).isEqualTo(12)
    }
}