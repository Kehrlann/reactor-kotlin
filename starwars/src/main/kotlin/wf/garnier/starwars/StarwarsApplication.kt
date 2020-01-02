package wf.garnier.starwars

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class StarwarsApplication {
    private val objectMapper = jacksonObjectMapper()

    @Bean
    fun people(): List<Person> {
		val file = javaClass.classLoader.getResourceAsStream("data/characters.json")!!
		return objectMapper.readValue(file)
    }

    @Bean
    fun films(): List<Film> {
        val file = javaClass.classLoader.getResourceAsStream("data/films.json")!!
        return objectMapper.readValue(file)
    }
}

fun main(args: Array<String>) {
    runApplication<StarwarsApplication>(*args)
}
