package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.repository.TicketRepository
import javax.inject.Inject

class GetEvents @Inject constructor(private val repository: TicketRepository) {
    suspend operator fun invoke(): List<Event> = repository.getEvents()
}