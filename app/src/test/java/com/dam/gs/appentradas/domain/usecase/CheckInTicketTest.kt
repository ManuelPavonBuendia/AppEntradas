package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.domain.repository.TicketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CheckInTicketTest {

    private val repository: TicketRepository = mock()
    private val checkInTicket = CheckInTicket(repository)

    companion object {
        const val TICKET_ID_TEST = 1
    }

    @Test
    fun checkInCorrecto() = runTest {
        checkInTicket(TICKET_ID_TEST)
        verify(repository).checkInTicket(TICKET_ID_TEST)
    }

    @Test
    fun checkInError() = runTest {
        whenever(repository.checkInTicket(TICKET_ID_TEST))
            .thenThrow(ConexionException())
        try {
            checkInTicket(TICKET_ID_TEST)
            assert(false)
        } catch (e: ConexionException) {
            assert(true)
        }
    }
}