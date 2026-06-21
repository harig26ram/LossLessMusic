package com.example.paytag.data

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val groupId: String = ""
)

data class Group(
    val groupId: String = "",
    val code: String = "",
    val members: List<String> = emptyList(),
    val memberNames: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
