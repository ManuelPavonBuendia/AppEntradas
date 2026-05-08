package com.dam.gs.appentradas.domain.usecase

import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.repository.TicketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class GetEventsTest {

    private val repository: TicketRepository = mock()
    private val getEvents = GetEvents(repository)

    companion object {
        const val EVENTO_TEST_NOMBRE = "Evento 1"
        const val EVENTO_TEST_NOMBRE_2 = "Evento 2"
    }

    @Test
    fun devuelveEventosCorrectos() = runTest {
        val eventos = listOf(
            Event(id = 1, nombre = EVENTO_TEST_NOMBRE),
            Event(id = 2, nombre = EVENTO_TEST_NOMBRE_2)
        )
        whenever(repository.getEvents()).thenReturn(eventos)
        val result = getEvents()
        assert(result == eventos)
    }

    @Test
    fun devuelveEventosVacios() = runTest {
        whenever(repository.getEvents()).thenReturn(emptyList())
        val result = getEvents()
        assert(result.isEmpty())
    }

    @Test
    fun devuelveEventosError() = runTest {
        whenever(repository.getEvents()).thenThrow(ConexionException())
        try {
            getEvents()
            assert(false)
        } catch (e: ConexionException) {
            assert(true)
        }
    }
}