package com.dam.gs.appentradas.domain.repository

import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.model.Ticket

interface TicketRepository {
    suspend fun authenticate(username: String, password: String)
    suspend fun logout()
    suspend fun getEvents(): List<Event>
    suspend fun descargarEntradasEvento(eventId: Int, onProgreso: ((Int) -> Unit)? = null)
    suspend fun descargarTodasLasEntradas(onProgreso: (Int) -> Unit)
    suspend fun hayEntradasLocales(eventId: Int): Boolean
    suspend fun validateTicket(code: String, eventId: Int, eventName: String): Ticket?
    suspend fun checkInTicket(ticketId: Int, barcode: String)
    suspend fun limpiarEntradasObsoletas()
}