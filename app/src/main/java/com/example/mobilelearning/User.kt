package com.example.mobilelearning

data class User(
    val id: String,
    val name: String,
    val role: String? = null,
    val nis: String? = null,
    val nip: String? = null,
    val kelompok: String? = null
)
