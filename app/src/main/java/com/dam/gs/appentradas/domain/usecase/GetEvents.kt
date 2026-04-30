package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.repository.TicketRepository

class GetEvents(private val repository: TicketRepository) {
    suspend operator fun invoke(): List<Event> = repository.getEvents()
}