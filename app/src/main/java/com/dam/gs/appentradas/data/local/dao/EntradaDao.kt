package com.dam.gs.appentradas.data.local.dao

import androidx.room.*
import com.dam.gs.appentradas.data.local.entity.EntradaEntity

@Dao
interface EntradaDao {

    @Query("SELECT * FROM entradas WHERE barcode = :barcode AND eventId = :eventId LIMIT 1")
    suspend fun getEntrada(barcode: String, eventId: Int): EntradaEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntradas(entradas: List<EntradaEntity>)

    @Query("""
        UPDATE entradas SET
        estado = :estado,
        haAsistido = :haAsistido,
        syncPendiente = CASE WHEN syncPendiente = 1 THEN 1 ELSE :syncPendiente END
        WHERE barcode = :barcode
    """)
    suspend fun actualizarEntrada(
        barcode: String,
        estado: String,
        haAsistido: Boolean,
        syncPendiente: Boolean
    )

    @Query("UPDATE entradas SET estado = 'done', haAsistido = 1, syncPendiente = 1, timestampCheckIn = :timestamp WHERE barcode = :barcode")
    suspend fun marcarAsistido(barcode: String, timestamp: Long)

    @Query("UPDATE entradas SET syncPendiente = 0 WHERE barcode = :barcode")
    suspend fun marcarSincronizada(barcode: String)

    @Query("SELECT * FROM entradas WHERE syncPendiente = 1")
    suspend fun getPendientesSync(): List<EntradaEntity>

    @Query("SELECT * FROM entradas WHERE syncPendiente = 1 AND eventId = :eventId")
    suspend fun getPendientesSyncPorEvento(eventId: Int): List<EntradaEntity>

    @Query("SELECT COUNT(*) FROM entradas WHERE eventId = :eventId")
    suspend fun countEntradasEvento(eventId: Int): Int

    @Query("DELETE FROM entradas WHERE eventId = :eventId")
    suspend fun borrarEntradasEvento(eventId: Int)
}