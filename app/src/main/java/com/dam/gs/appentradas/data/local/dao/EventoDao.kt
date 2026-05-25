package com.dam.gs.appentradas.data.local.dao

import androidx.room.*
import com.dam.gs.appentradas.data.local.entity.EventoEntity

@Dao
interface EventoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventos(eventos: List<EventoEntity>)

    @Query("SELECT * FROM eventos")
    suspend fun getEventos(): List<EventoEntity>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun getEvento(id: Int): EventoEntity?

    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun borrarEvento(id: Int)

    @Query("DELETE FROM eventos")
    suspend fun borrarTodos()
}