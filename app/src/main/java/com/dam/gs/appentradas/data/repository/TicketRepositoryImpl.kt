package com.dam.gs.appentradas.data.repository

import com.dam.gs.appentradas.core.AppConstants
import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.model.Ticket
import com.dam.gs.appentradas.domain.repository.TicketRepository
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TicketRepositoryImpl : TicketRepository {

    private var uid: Int = 0
    private var password: String = ""

    suspend fun authenticate(username: String, password: String) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val config = XmlRpcClientConfigImpl().apply {
                serverURL = URL("${AppConstants.URL_ODOO}/xmlrpc/2/common")
            }
            val client = XmlRpcClient().apply { setConfig(config) }
            uid = (client.execute("authenticate", arrayOf(
                AppConstants.DB_NAME, username, password, emptyMap<String, Any>()
            )) as Int)
            this@TicketRepositoryImpl.password = password
        }
    }

    private suspend fun callKw(model: String, method: String, args: Array<Any>, kwargs: Map<String, Any>): Any {
        return withContext(Dispatchers.IO) {
            val config = XmlRpcClientConfigImpl().apply {
                serverURL = URL("${AppConstants.URL_ODOO}/xmlrpc/2/object")
            }
            val client = XmlRpcClient().apply { setConfig(config) }
            client.execute("execute_kw", arrayOf(
                AppConstants.DB_NAME, uid, password, model, method, args, kwargs
            ))
        }
    }

    override suspend fun getEvents(): List<Event> {
        val result = callKw(
            model = AppConstants.MODEL_EVENTO,
            method = AppConstants.METHOD_SEARCH_READ,
            args = arrayOf(emptyArray<Any>()),
            kwargs = mapOf("fields" to listOf(AppConstants.FIELD_ID, AppConstants.FIELD_NAME))
        ) as Array<*>

        return result.map { item ->
            val map = item as Map<*, *>
            Event(
                id = (map[AppConstants.FIELD_ID] as Int),
                nombre = (map[AppConstants.FIELD_NAME] as String)
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
                "fields" to listOf(AppConstants.FIELD_ID, AppConstants.FIELD_NAME, AppConstants.FIELD_STATE, AppConstants.FIELD_PARTNER_ID),
                "limit" to 1
            )
        ) as Array<*>

        if (result.isEmpty()) return null

        val t = result[0] as Map<*, *>
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
            estado = t[AppConstants.FIELD_STATE] as String
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