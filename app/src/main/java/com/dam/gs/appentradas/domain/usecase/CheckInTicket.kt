package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.domain.repository.TicketRepository
import javax.inject.Inject

class CheckInTicket @Inject constructor(private val repository: TicketRepository) {
    suspend operator fun invoke(ticketId: Int, barcode: String) =
        repository.checkInTicket(ticketId, barcode)
}