package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.domain.model.EstadoTicket
import com.dam.gs.appentradas.domain.model.Ticket
import com.dam.gs.appentradas.domain.repository.TicketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ValidateTicketTest {

    private val repository: TicketRepository = mock()
    private val validateTicket = ValidateTicket(repository)

    companion object {
        const val BARCODE_TEST = "ABC123"
        const val EVENTO_ID_TEST = 1
        const val EVENTO_NOMBRE_TEST = "Evento 1"
        const val TICKET_NOMBRE_TEST = "Ticket 1"
        const val TICKET_CLIENTE_TEST = "Cliente 1"
    }

    private val ticketValido = Ticket(
        id = 1,
        nombre = TICKET_NOMBRE_TEST,
        cliente = TICKET_CLIENTE_TEST,
        evento = EVENTO_NOMBRE_TEST,
        estado = EstadoTicket.OPEN
    )

    @Test
    fun  validateTicketCorrecto() = runTest {
        whenever(repository.validateTicket(BARCODE_TEST, EVENTO_ID_TEST, EVENTO_NOMBRE_TEST))
            .thenReturn(ticketValido)
        val result = validateTicket(BARCODE_TEST, EVENTO_ID_TEST, EVENTO_NOMBRE_TEST)
        assert(result == ticketValido)
    }

    @Test
    fun  validateTicketNull() = runTest {
        whenever(repository.validateTicket(BARCODE_TEST, EVENTO_ID_TEST, EVENTO_NOMBRE_TEST))
            .thenReturn(null)
        val result = validateTicket(BARCODE_TEST, EVENTO_ID_TEST, EVENTO_NOMBRE_TEST)
        assert(result == null)
    }

    @Test
    fun validateTicketError() = runTest {
        whenever(repository.validateTicket(BARCODE_TEST, EVENTO_ID_TEST, EVENTO_NOMBRE_TEST))
            .thenThrow(ConexionException())
        try {
            validateTicket(BARCODE_TEST, EVENTO_ID_TEST, EVENTO_NOMBRE_TEST)
            assert(false)
        } catch (e: ConexionException) {
            assert(true)
        }
    }
}