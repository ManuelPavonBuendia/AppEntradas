package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.domain.model.Ticket
import com.dam.gs.appentradas.domain.repository.TicketRepository

class ValidateTicket(private val repository: TicketRepository) {
    suspend operator fun invoke(code: String, eventId: Int, eventName: String): Ticket? =
        repository.validateTicket(code, eventId, eventName)
}