package wf.garnier.starwars

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class StarwarsControllerTest {
    val people = listOf(
            Person("One", emptyList(), "/api/people/1/"),
            Person("Two", emptyList(), "/api/people/2/")
    ).associateBy { it.resourceId() }
    val subject = StarwarsController(people, emptyMap())

    @Test
    fun getPerson() {
        val response = subject.getPerson(1).block()

        assertThat(response!!.body!!.name).isEqualTo("One")
    }

    @Test
    fun `when person is not available, it returns 404`() {
        val response = subject.getPerson(99).block()

        assertThat(response!!.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}