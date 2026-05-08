package com.dam.gs.appentradas.domain.model

import com.dam.gs.appentradas.core.constants.AppConstants

enum class EstadoTicket {
    OPEN, DONE, CANCELLED, UNKNOWN;
    companion object {
        fun fromString(value: String): EstadoTicket = when (value) {
            AppConstants.STATE_OPEN -> OPEN
            AppConstants.STATE_DONE -> DONE
            AppConstants.STATE_CANCELLED -> CANCELLED
            else -> UNKNOWN
        }
    }
}