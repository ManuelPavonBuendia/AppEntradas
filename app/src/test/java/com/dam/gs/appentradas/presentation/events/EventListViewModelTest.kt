package com.dam.gs.appentradas.presentation.events

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.usecase.GetEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class EventListViewModelTest {
    companion object {
        const val EVENTO_TEST_NOMBRE = "Evento 1"
        const val EVENTO_TEST_NOMBRE_2 = "Evento 2"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val getEvents: GetEvents = mock()
    private lateinit var viewModel: EventListViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EventListViewModel(getEvents)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun devuelveEventos() = runTest {
        val eventos = listOf(
            Event(id = 1, nombre = EVENTO_TEST_NOMBRE),
            Event(id = 2, nombre = EVENTO_TEST_NOMBRE_2)
        )
        whenever(getEvents()).thenReturn(eventos)
        viewModel.loadEvents()
        assert(viewModel.events.value == eventos)
    }

    @Test
    fun devuelveError() = runTest {
        whenever(getEvents()).thenThrow(ConexionException())
        viewModel.loadEvents()
        assert(viewModel.error.value == AppConstants.ERROR_CARGAR_EVENTOS)
    }

    @Test
    fun devuelveVacio() = runTest {
        whenever(getEvents()).thenReturn(emptyList())
        viewModel.loadEvents()
        assert(viewModel.events.value?.isEmpty() == true)
    }
}