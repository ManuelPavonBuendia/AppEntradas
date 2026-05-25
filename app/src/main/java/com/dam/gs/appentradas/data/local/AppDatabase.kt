package com.dam.gs.appentradas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dam.gs.appentradas.data.local.dao.EntradaDao
import com.dam.gs.appentradas.data.local.dao.EventoDao
import com.dam.gs.appentradas.data.local.dao.SesionDao
import com.dam.gs.appentradas.data.local.entity.EntradaEntity
import com.dam.gs.appentradas.data.local.entity.EventoEntity
import com.dam.gs.appentradas.data.local.entity.SesionEntity

@Database(
    entities = [
        EntradaEntity::class,
        EventoEntity::class,
        SesionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entradaDao(): EntradaDao
    abstract fun eventoDao(): EventoDao
    abstract fun sesionDao(): SesionDao
}