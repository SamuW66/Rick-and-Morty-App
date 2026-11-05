package com.danidev.apprickmorty.data.model

data class  CharacterResponse(
    val results: List<RickCharacter>
)

data class RickCharacter(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String = "",
    val image: String,
    val origin: Origin,
    val location: Origin = Origin(name = "Desconocida")
)

data class Origin(
    val name: String
)
