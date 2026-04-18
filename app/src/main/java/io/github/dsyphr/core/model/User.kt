package io.github.dsyphr.core.model

data class User(
    val username: String,
    val uid: String,
    val email: String = ""
)
