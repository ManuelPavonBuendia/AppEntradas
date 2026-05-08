package com.dam.gs.appentradas.data.repository

import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.domain.model.EstadoTicket
import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.model.Ticket
import com.dam.gs.appentradas.domain.repository.TicketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.net.URL
import com.dam.gs.appentradas.core.exceptions.CredencialesInvalidasException
import com.dam.gs.appentradas.core.exceptions.ConexionException

class TicketRepositoryImpl : TicketRepository {

    private var uid: Int = 0
    private var password: String = ""

    override suspend fun authenticate(username: String, password: String) {
        withContext(Dispatchers.IO) {
            val config = XmlRpcClientConfigImpl().apply {
                serverURL = URL("${AppConstants.URL_ODOO}${AppConstants.XMLRPC_COMMON}")
            }
            val client = XmlRpcClient().apply { setConfig(config) }
            val result = client.execute(
                AppConstants.METHOD_AUTHENTICATE,
                arrayOf(AppConstants.DB_NAME, username, password, emptyMap<String, Any>())
            )
            if (result is Boolean && !result) {
                throw CredencialesInvalidasException()
            }
            uid = result as? Int ?: throw ConexionException()
            this@TicketRepositoryImpl.password = password
        }
    }

    override suspend fun logout() {
        uid = 0
        password = ""
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
            }catch (e: Exception){
                throw ConexionException()
            }
        }
    }

    override suspend fun getEvents(): List<Event> {
        val result = callKw(
            model = AppConstants.MODEL_EVENTO,
            method = AppConstants.METHOD_SEARCH_READ,
            args = arrayOf(arrayOf(arrayOf(AppConstants.FIELD_STAGE_ID_NAME, AppConstants.OPERATOR_EQUALS, AppConstants.STAGE_ANUNCIADO))),
            kwargs = mapOf(AppConstants.FIELDS to listOf(AppConstants.FIELD_ID, AppConstants.FIELD_NAME, AppConstants.FIELD_IMAGE))
        ) as Array<*>

        return result.map { item ->
            val map = item as Map<*, *>
            Event(
                id = map[AppConstants.FIELD_ID] as Int,
                nombre = map[AppConstants.FIELD_NAME] as String,
                imagen = map[AppConstants.FIELD_IMAGE] as? String
            )
        }
    }

    override suspend fun validateTicket(code: String, eventId: Int, eventName: String): Ticket? {
        val result = callKw(
            model = AppConstants.MODEL_REGISTRO,
            method = AppConstants.METHOD_SEARCH_READ,
            args = arrayOf(arrayOf(
                arrayOf(AppConstants.FIELD_BARCODE, AppConstants.OPERATOR_EQUALS, code),
                arrayOf(AppConstants.FIELD_EVENT_ID, AppConstants.OPERATOR_EQUALS, eventId)
            )),
            kwargs = mapOf(
                AppConstants.FIELDS to listOf(AppConstants.FIELD_ID, AppConstants.FIELD_NAME, AppConstants.FIELD_STATE, AppConstants.FIELD_PARTNER_ID),
                AppConstants.LIMIT to AppConstants.LIMIT_ONE
            )
        ) as Array<*>

        if (result.isEmpty()) return null
        return mapToTicket(result[0] as Map<*, *>, eventName)
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

    override suspend fun checkInTicket(ticketId: Int) {
        callKw(
            model = AppConstants.MODEL_REGISTRO,
            method = AppConstants.METHOD_SET_DONE,
            args = arrayOf(arrayOf(ticketId)),
            kwargs = emptyMap()
        )
    }
}