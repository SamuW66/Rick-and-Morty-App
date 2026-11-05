package com.danidev.apprickmorty.data.remote

import com.danidev.apprickmorty.data.model.CharacterResponse
import com.danidev.apprickmorty.data.model.RickCharacter
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(
        @Query("name") name : String? = null
    ): CharacterResponse

    @GET("character/{id}")
    suspend fun getCharacterById(
        @Path("id") id : Int
    ): RickCharacter
}

object RetrofitClient {
    private const val  BASE_URL = "https://rickandmortyapi.com/api/"

    val api: RickAndMortyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RickAndMortyApi::class.java)
    }
}