package com.dam.gs.appentradas.domain.model

enum class EstadoTicket {
    OPEN, DONE, CANCELLED, UNKNOWN;
    companion object {
        fun fromString(value: String): EstadoTicket = when (value) {
            "open" -> OPEN
            "done" -> DONE
            "cancel" -> CANCELLED
            else -> UNKNOWN
        }
    }
}