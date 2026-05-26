package com.dam.gs.appentradas.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.core.exceptions.CredencialesInvalidasException
import com.dam.gs.appentradas.data.local.dao.EntradaDao
import com.dam.gs.appentradas.data.local.dao.EventoDao
import com.dam.gs.appentradas.data.local.dao.SesionDao
import com.dam.gs.appentradas.data.local.entity.EntradaEntity
import com.dam.gs.appentradas.data.local.entity.SesionEntity
import com.dam.gs.appentradas.data.local.mapper.toEntity
import com.dam.gs.appentradas.data.local.mapper.toEvent
import com.dam.gs.appentradas.data.local.mapper.toTicket
import com.dam.gs.appentradas.domain.model.EstadoTicket
import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.model.Ticket
import com.dam.gs.appentradas.domain.repository.TicketRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import org.mindrot.jbcrypt.BCrypt
import java.net.URL
import javax.inject.Inject
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dam.gs.appentradas.data.worker.SyncWorker


class TicketRepositoryImpl @Inject constructor(
    private val entradaDao: EntradaDao,
    private val eventoDao: EventoDao,
    private val sesionDao: SesionDao,
    @ApplicationContext private val context: Context
) : TicketRepository {

    private var uid: Int = 0
    private var password: String = ""

    private fun hayConexion(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork?.let {
            cm.getNetworkCapabilities(it)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    }

    private fun clienteCommon(): XmlRpcClient {
        val config = XmlRpcClientConfigImpl().apply {
            serverURL = URL("${AppConstants.URL_ODOO}${AppConstants.XMLRPC_COMMON}")
        }
        return XmlRpcClient().apply { setConfig(config) }
    }

    private suspend fun callKw(
        model: String,
        method: String,
        args: Array<Any>,
        kwargs: Map<String, Any>
    ): Any {
        return withContext(Dispatchers.IO) {
            val config = XmlRpcClientConfigImpl().apply {
                serverURL = URL("${AppConstants.URL_ODOO}${AppConstants.XMLRPC_OBJECT}")
            }
            val client = XmlRpcClient().apply { setConfig(config) }
            try {
                client.execute(
                    AppConstants.METHOD_EXECUTE_KW,
                    arrayOf(AppConstants.DB_NAME, uid, password, model, method, args, kwargs)
                )
            } catch (e: Exception) {
                throw ConexionException()
            }
        }
    }

    override suspend fun authenticate(username: String, password: String) {
        if (hayConexion()) {
            withContext(Dispatchers.IO) {
                val result = clienteCommon().execute(
                    AppConstants.METHOD_AUTHENTICATE,
                    arrayOf(AppConstants.DB_NAME, username, password, emptyMap<String, Any>())
                )
                if (result is Boolean && !result) throw CredencialesInvalidasException()
                uid = result as? Int ?: throw ConexionException()
                this@TicketRepositoryImpl.password = password

                val hash = BCrypt.hashpw(password, BCrypt.gensalt())
                sesionDao.guardarSesion(
                    SesionEntity(
                        uid = uid,
                        username = username,
                        passwordHash = hash,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        } else {
            val sesion = sesionDao.getSesion() ?: throw ConexionException()
            if (sesion.username != username) throw CredencialesInvalidasException()
            if (!BCrypt.checkpw(password, sesion.passwordHash)) throw CredencialesInvalidasException()
            uid = sesion.uid
            this.password = password
        }
    }

    override suspend fun logout() {
        uid = 0
        password = ""
    }


    override suspend fun getEvents(): List<Event> {
        return if (hayConexion()) {
            val result = callKw(
                model = AppConstants.MODEL_EVENTO,
                method = AppConstants.METHOD_SEARCH_READ,
                args = arrayOf(arrayOf(arrayOf(
                    AppConstants.FIELD_STAGE_ID_NAME,
                    AppConstants.OPERATOR_EQUALS,
                    AppConstants.STAGE_ANUNCIADO
                ))),
                kwargs = mapOf(
                    AppConstants.FIELDS to listOf(
                        AppConstants.FIELD_ID,
                        AppConstants.FIELD_NAME,
                        AppConstants.FIELD_IMAGE,
                        "date_end",
                        "stage_id"
                    )
                )
            ) as Array<*>

            val eventos = result.map { item ->
                val map = item as Map<*, *>
                Event(
                    id = map[AppConstants.FIELD_ID] as Int,
                    nombre = map[AppConstants.FIELD_NAME] as String,
                    imagen = map[AppConstants.FIELD_IMAGE] as? String
                )
            }

            eventoDao.insertEventos(eventos.map { it.toEntity() })
            eventos

        } else {
            eventoDao.getEventos().map { it.toEvent() }
        }
    }


    override suspend fun descargarEntradasEvento(
        eventId: Int,
        onProgreso: ((Int) -> Unit)?
    ) {
        if (!hayConexion()) return

        val result = callKw(
            model = AppConstants.MODEL_REGISTRO,
            method = AppConstants.METHOD_SEARCH_READ,
            args = arrayOf(arrayOf(
                arrayOf(AppConstants.FIELD_EVENT_ID, AppConstants.OPERATOR_EQUALS, eventId)
            )),
            kwargs = mapOf(
                AppConstants.FIELDS to listOf(
                    AppConstants.FIELD_ID,
                    AppConstants.FIELD_NAME,
                    AppConstants.FIELD_STATE,
                    AppConstants.FIELD_PARTNER_ID,
                    AppConstants.FIELD_BARCODE
                )
            )
        ) as Array<*>

        val entradas = mapearEntradas(result, eventId)
        val pendientes = entradaDao.getPendientesSyncPorEvento(eventId).map { it.barcode }.toSet()
        entradaDao.insertEntradas(entradas.filter { it.barcode !in pendientes })
    }

    private fun mapearEntradas(result: Array<*>, eventId: Int): List<EntradaEntity> {
        return result.mapNotNull { item ->
            val map = item as Map<*, *>
            val barcode = map[AppConstants.FIELD_BARCODE] as? String ?: return@mapNotNull null
            val partnerId = map[AppConstants.FIELD_PARTNER_ID]
            val nombreCliente = if (partnerId is Array<*> && partnerId.size > 1) {
                partnerId[1] as String
            } else {
                AppConstants.SIN_NOMBRE
            }
            EntradaEntity(
                barcode = barcode,
                eventId = eventId,
                entradaId = map[AppConstants.FIELD_ID] as Int,
                nombreEntrada = map[AppConstants.FIELD_NAME] as String,
                nombreComprador = nombreCliente,
                nombreAsistente = nombreCliente,
                estado = map[AppConstants.FIELD_STATE] as String
            )
        }
    }

    override suspend fun descargarTodasLasEntradas(onProgreso: (Int) -> Unit) {
        val eventos = eventoDao.getEventos()
        eventos.forEachIndexed { index, evento ->
            descargarEntradasEvento(evento.id)
            val progreso = ((index + 1) * 100) / eventos.size
            onProgreso(progreso)
        }
    }

    override suspend fun hayEntradasLocales(eventId: Int): Boolean {
        return entradaDao.countEntradasEvento(eventId) > 0
    }


    override suspend fun validateTicket(code: String, eventId: Int, eventName: String): Ticket? {
        return if (hayConexion()) {
            val result = callKw(
                model = AppConstants.MODEL_REGISTRO,
                method = AppConstants.METHOD_SEARCH_READ,
                args = arrayOf(arrayOf(
                    arrayOf(AppConstants.FIELD_BARCODE, AppConstants.OPERATOR_EQUALS, code),
                    arrayOf(AppConstants.FIELD_EVENT_ID, AppConstants.OPERATOR_EQUALS, eventId)
                )),
                kwargs = mapOf(
                    AppConstants.FIELDS to listOf(
                        AppConstants.FIELD_ID,
                        AppConstants.FIELD_NAME,
                        AppConstants.FIELD_STATE,
                        AppConstants.FIELD_PARTNER_ID
                    ),
                    AppConstants.LIMIT to AppConstants.LIMIT_ONE
                )
            ) as Array<*>

            if (result.isEmpty()) return null
            val ticket = mapToTicket(result[0] as Map<*, *>, eventName)

            val local = entradaDao.getEntrada(code, eventId)
            if (local == null || !local.syncPendiente) {
                entradaDao.insertEntradas(listOf(ticket.toEntity(code, eventId)))
            }
            ticket

        } else {
            entradaDao.getEntrada(code, eventId)?.toTicket(eventName)
        }
    }

    override suspend fun checkInTicket(ticketId: Int, barcode: String) {
        entradaDao.marcarAsistido(barcode, System.currentTimeMillis())

        if (hayConexion()) {
            try {
                callKw(
                    model = AppConstants.MODEL_REGISTRO,
                    method = AppConstants.METHOD_SET_DONE,
                    args = arrayOf(arrayOf(ticketId)),
                    kwargs = emptyMap()
                )
                entradaDao.marcarSincronizada(barcode)
            } catch (e: ConexionException) {
                lanzarSyncWorker()
            }
        }else{
            lanzarSyncWorker()
        }
    }

    private fun lanzarSyncWorker() {
        val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            AppConstants.SYNC_WORKER_NAME,
            ExistingWorkPolicy.KEEP,
            syncWork
        )
    }


    override suspend fun limpiarEntradasObsoletas() {
        val ahora = System.currentTimeMillis()
        val margen24h = 24 * 60 * 60 * 1000L

        eventoDao.getEventos().forEach { evento ->
            val terminado = evento.estado != AppConstants.STAGE_ANUNCIADO
            val pasaron24h = evento.fechaFin > 0 &&
                    (ahora - evento.fechaFin) > margen24h

            if (terminado && pasaron24h) {
                entradaDao.borrarEntradasEvento(evento.id)
                eventoDao.borrarEvento(evento.id)
            }
        }
    }


    private fun mapToTicket(t: Map<*, *>, eventName: String): Ticket {
        val partnerId = t[AppConstants.FIELD_PARTNER_ID]
        val cliente = if (partnerId is Array<*> && partnerId.size > 1) {
            partnerId[1] as String
        } else {
            AppConstants.SIN_NOMBRE
        }
        return Ticket(
            id = t[AppConstants.FIELD_ID] as Int,
            nombre = t[AppConstants.FIELD_NAME] as String,
            cliente = cliente,
            evento = eventName,
            estado = EstadoTicket.fromString(t[AppConstants.FIELD_STATE] as String)
        )
    }
}