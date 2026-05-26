package com.dam.gs.appentradas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dam.gs.appentradas.core.constants.AppConstants

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val imagen: String?,
    val fechaFin: Long,
    val estado: String
)