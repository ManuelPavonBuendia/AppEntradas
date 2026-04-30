package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.domain.repository.TicketRepository

class CheckInTicket(private val repository: TicketRepository) {
    suspend operator fun invoke(ticketId: Int) = repository.checkInTicket(ticketId)
}