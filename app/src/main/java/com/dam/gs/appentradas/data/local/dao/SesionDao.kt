package com.dam.gs.appentradas.data.local.dao

import androidx.room.*
import com.dam.gs.appentradas.data.local.entity.SesionEntity

@Dao
interface SesionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSesion(sesion: SesionEntity)

    @Query("SELECT * FROM sesion WHERE id = 1 LIMIT 1")
    suspend fun getSesion(): SesionEntity?

    @Query("DELETE FROM sesion")
    suspend fun borrarSesion()
}