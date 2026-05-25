package com.dam.gs.appentradas.data.local.mapper

import com.dam.gs.appentradas.data.local.entity.EntradaEntity
import com.dam.gs.appentradas.data.local.entity.EventoEntity
import com.dam.gs.appentradas.domain.model.EstadoTicket
import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.model.Ticket

fun EventoEntity.toEvent() = Event(
    id = id,
    nombre = nombre,
    imagen = imagen
)

fun Event.toEntity(fechaFin: Long = 0L, estado: String = "Announced") = EventoEntity(
    id = id,
    nombre = nombre,
    imagen = imagen,
    fechaFin = fechaFin,
    estado = estado
)

fun EntradaEntity.toTicket(eventName: String) = Ticket(
    id = entradaId,
    nombre = nombreEntrada,
    cliente = nombreAsistente,
    evento = eventName,
    estado = EstadoTicket.fromString(estado)
)

fun Ticket.toEntity(barcode: String, eventId: Int) = EntradaEntity(
    barcode = barcode,
    eventId = eventId,
    entradaId = id,
    nombreEntrada = nombre,
    nombreComprador = cliente,
    nombreAsistente = cliente,
    estado = when (estado) {
        EstadoTicket.OPEN -> "open"
        EstadoTicket.DONE -> "done"
        EstadoTicket.CANCELLED -> "cancel"
        EstadoTicket.UNKNOWN -> "unknown"
    }
)