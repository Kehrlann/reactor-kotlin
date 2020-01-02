package wf.garnier.starwars

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class StarwarsApplication {
    private val objectMapper = jacksonObjectMapper()

    @Bean
    fun people(): Map<Int, Person> {
		val file = javaClass.classLoader.getResourceAsStream("data/characters.json")!!
        val map = objectMapper.readValue<List<Person>>(file).associateBy { it.resourceId() }
        return ConcurrentHashMap(map)
    }

    @Bean
    fun films(): Map<Int, Film> {
        val file = javaClass.classLoader.getResourceAsStream("data/films.json")!!
        val map = objectMapper.readValue<List<Film>>(file).associateBy { it.resourceId() }
        return ConcurrentHashMap(map)
    }
}

fun main(args: Array<String>) {
    runApplication<StarwarsApplication>(*args)
}
