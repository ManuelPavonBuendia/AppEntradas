package com.dam.gs.appentradas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entradas")
data class EntradaEntity(
    @PrimaryKey val barcode: String,
    val eventId: Int,
    val entradaId: Int,
    val nombreEntrada: String,
    val nombreComprador: String,
    val nombreAsistente: String,
    val estado: String,
    val haAsistido: Boolean = false,
    val syncPendiente: Boolean = false,
    val timestampCheckIn: Long? = null
)