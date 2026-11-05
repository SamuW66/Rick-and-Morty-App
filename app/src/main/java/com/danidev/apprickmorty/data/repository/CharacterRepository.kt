package com.danidev.apprickmorty.data.repository

import com.danidev.apprickmorty.data.model.RickCharacter
import com.danidev.apprickmorty.data.remote.RetrofitClient

class CharacterRepository {

    private val api = RetrofitClient.api

    suspend fun getCharacters(name: String?=null): Result<List<RickCharacter>>{
        return  try {
            val response = api.getCharacters(name= name)
            Result.success(response.results)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun getCharacterById(id: Int): Result<RickCharacter> {
        return try {
            Result.success(api.getCharacterById(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}