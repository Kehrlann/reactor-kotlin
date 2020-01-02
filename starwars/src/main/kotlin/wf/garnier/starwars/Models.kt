package wf.garnier.starwars


data class Person(val name: String, val url: String, val films: List<String>)

data class Film(val title: String, val url: String, val characters: List<String>)