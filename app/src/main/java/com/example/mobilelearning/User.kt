package com.example.mobilelearning

data class User(
    val id: String,
    val username: String,
    val name: String,
    val role: String? = null,
    val nis: String? = null,
    val nip: String? = null,
    val kelompok_id: String? = null, // ID kelompok
    val kelompok: String? = null // Nama kelompok
)


