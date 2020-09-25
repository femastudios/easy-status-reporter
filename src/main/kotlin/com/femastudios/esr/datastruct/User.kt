package com.femastudios.esr.datastruct

data class User(val username: String, val password: String) {

    override fun equals(other: Any?): Boolean = other is User && other.username == username

    override fun hashCode() = username.hashCode()
}