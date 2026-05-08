package com.dam.gs.appentradas.domain.model

import com.dam.gs.appentradas.core.constants.AppConstants
import org.junit.Test

class EstadoTicketTest {

    companion object {
        const val UNKNOWN_STATE_TEST = "valor_desconocido"
    }

    @Test
    fun devuelveAbierto() {
        assert(EstadoTicket.fromString(AppConstants.STATE_OPEN) == EstadoTicket.OPEN)
    }

    @Test
    fun devuelveHecho() {
        assert(EstadoTicket.fromString(AppConstants.STATE_DONE) == EstadoTicket.DONE)
    }

    @Test
    fun devuelveCancelado() {
        assert(EstadoTicket.fromString(AppConstants.STATE_CANCELLED) == EstadoTicket.CANCELLED)
    }

    @Test
    fun devuelveDesconocido() {
        assert(EstadoTicket.fromString(UNKNOWN_STATE_TEST) == EstadoTicket.UNKNOWN)
    }
}