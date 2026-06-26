package com.example.testingmyapi.model

import com.google.gson.annotations.SerializedName

data class Character(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("Image")
    val image: String? = null,
    @SerializedName("Image1")
    val image1: String? = null,
    @SerializedName("Image2")
    val image2: String? = null,
    @SerializedName("Quote")
    val quote: String? = null,
    @SerializedName("caption")
    val caption: String? = null,
    @SerializedName("caption1")
    val caption1: String? = null,
    @SerializedName("caption2")
    val caption3: String? = null,
    @SerializedName("Description")
    val description: String? = null,
    @SerializedName("Description1")
    val description1: String? = null,
    @SerializedName("Description2")
    val description2: String? = null,
    @SerializedName("Description3")
    val description3: String? = null,
    @SerializedName("Url_Video")
    val urlVideo: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("language")
    val language: String? = null
)

data class CharacterDetail(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("Image")
    val image: String? = null,
    @SerializedName("Image1")
    val image1: String? = null,
    @SerializedName("Image2")
    val image2: String? = null,
    @SerializedName("Quote")
    val quote: String? = null,
    @SerializedName("caption")
    val caption: String? = null,
    @SerializedName("caption1")
    val caption1: String? = null,
    @SerializedName("caption2")
    val caption2: String? = null,
    @SerializedName("Description")
    val description: String? = null,
    @SerializedName("Description1")
    val description1: String? = null,
    @SerializedName("Description2")
    val description2: String? = null,
    @SerializedName("Description3")
    val description3: String? = null,
    @SerializedName("Url_Video")
    val urlVideo: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("language")
    val language: String? = null
)

data class Category(
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("totalItems")
    val totalItems: Int? = null
)

data class Language(
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("totalItems")
    val totalItems: Int? = null
)