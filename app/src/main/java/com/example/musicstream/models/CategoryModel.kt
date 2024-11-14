package com.example.musicstream.models

data class CategoryModel(
    val name: String = "",
    val coverUrl: String = "",
    var songs: List<String> = listOf()
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", listOf())
}
