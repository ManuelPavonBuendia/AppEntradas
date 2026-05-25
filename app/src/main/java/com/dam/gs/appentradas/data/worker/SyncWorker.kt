package com.dam.gs.appentradas.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dam.gs.appentradas.data.local.dao.EntradaDao
import com.dam.gs.appentradas.domain.repository.TicketRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val entradaDao: EntradaDao,
    private val repository: TicketRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val pendientes = entradaDao.getPendientesSync()

            for (entrada in pendientes) {
                repository.checkInTicket(entrada.entradaId, entrada.barcode)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}