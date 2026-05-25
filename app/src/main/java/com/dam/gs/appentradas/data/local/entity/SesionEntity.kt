package com.dam.gs.appentradas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sesion")
data class SesionEntity(
    @PrimaryKey val id: Int = 1,
    val uid: Int,
    val username: String,
    val passwordHash: String,
    val timestamp: Long
)