package com.example.testingmyapi.api

import com.example.testingmyapi.model.Category
import com.example.testingmyapi.model.Character
import com.example.testingmyapi.model.CharacterDetail
import com.example.testingmyapi.model.Language
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/information")
    suspend fun getInformation(
        @Query("limit") limit: Int = 37,
        @Query("offset") offset: Int = 0,
        @Query("category") category: String? = null,
        @Query("language") language: String? = null,
        @Query("name") name: String? = null
    ): Response<List<Character>>

    @GET("api/information/{id}")
    suspend fun getInformationById(
        @Path("id") id: String
    ): Response<CharacterDetail>

    @GET("api/categories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("api/languages")
    suspend fun getLanguages(): Response<List<Language>>
}