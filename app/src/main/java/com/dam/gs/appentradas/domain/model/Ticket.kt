package com.dam.gs.appentradas.domain.model

data class Ticket(
    val id: Int,
    val nombre: String,
    val cliente: String,
    val evento: String,
    val estado: String
)