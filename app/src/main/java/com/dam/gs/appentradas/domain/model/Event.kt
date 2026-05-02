package com.dam.gs.appentradas.domain.model

data class Event(
    val id: Int,
    val nombre: String,
    val imagen: String? = null
)