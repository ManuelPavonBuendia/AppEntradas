package com.dam.gs.appentradas.presentation.scanner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.domain.model.EstadoTicket
import com.dam.gs.appentradas.domain.model.Ticket
import com.dam.gs.appentradas.domain.usecase.CheckInTicket
import com.dam.gs.appentradas.domain.usecase.ValidateTicket
import junit.framework.TestCase.assertTrue
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ScannerViewModelTest {
    companion object {
        const val TICKET_NOMBRE = "Ticket 1"
        const val TICKET_CLIENTE = "Cliente 1"
        const val BARCODE_TEST = "ABC123"
        const val EVENTO_TEST_NOMBRE = "Evento 1"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val validateTicket: ValidateTicket = mock()
    private val checkInTicket: CheckInTicket = mock()
    private lateinit var viewModel: ScannerViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val ticketValido = Ticket(
        id = 1,
        nombre = TICKET_NOMBRE,
        cliente = TICKET_CLIENTE,
        evento = EVENTO_TEST_NOMBRE,
        estado = EstadoTicket.OPEN
    )

    private val ticketUsado = ticketValido.copy(estado = EstadoTicket.DONE)
    private val ticketCancelado = ticketValido.copy(estado = EstadoTicket.CANCELLED)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScannerViewModel(validateTicket, checkInTicket)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun ticketValidoProduceValido() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)).thenReturn(ticketValido)
        whenever(checkInTicket(ticketValido.id, BARCODE_TEST)).thenReturn(Unit)
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        val state = viewModel.scanState.value as ScannerViewModel.ScanState.Valid
        assertEquals(ticketValido.nombre, state.nombre)
        assertEquals(ticketValido.cliente, state.cliente)
    }

    @Test
    fun ticketUsadoProduceYaUtilizado() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)).thenReturn(ticketUsado)
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assertTrue(viewModel.scanState.value is ScannerViewModel.ScanState.AlreadyUsed)
        verify(checkInTicket, never()).invoke(any(), any())
    }

    @Test
    fun ticketCanceladoProduceInvalido() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)).thenReturn(ticketCancelado)
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assertTrue(viewModel.scanState.value is ScannerViewModel.ScanState.Invalid)
        verify(checkInTicket, never()).invoke(any(), any())
    }

    @Test
    fun ticketNoEncontradoProduceInvalido() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)).thenReturn(null)
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assertTrue(viewModel.scanState.value is ScannerViewModel.ScanState.Invalid)
    }

    @Test
    fun checkInFallaYRevalidacionConfirmaDone() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE))
            .thenReturn(ticketValido)
            .thenReturn(ticketUsado)
        whenever(checkInTicket(ticketValido.id, BARCODE_TEST)).thenThrow(ConexionException())
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assertTrue(viewModel.scanState.value is ScannerViewModel.ScanState.Valid)
    }

    @Test
    fun checkInFallaYRevalidacionConfirmaOpen() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE))
            .thenReturn(ticketValido)
            .thenReturn(ticketValido)
        whenever(checkInTicket(ticketValido.id, BARCODE_TEST)).thenThrow(ConexionException())
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assertTrue(viewModel.scanState.value is ScannerViewModel.ScanState.Error)
    }

    @Test
    fun ticketValidoSinRed() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE))
            .thenReturn(ticketValido)
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assert(viewModel.scanState.value is ScannerViewModel.ScanState.Valid)
    }

    @Test
    fun ticketUsadoSinRed() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE))
            .thenReturn(ticketUsado)
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assert(viewModel.scanState.value is ScannerViewModel.ScanState.AlreadyUsed)
    }

    @Test
    fun ticketNoEncontradoSinRed() = runTest {
        whenever(validateTicket(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE))
            .thenReturn(null)
        viewModel.handleScan(BARCODE_TEST, 1, EVENTO_TEST_NOMBRE)
        assert(viewModel.scanState.value is ScannerViewModel.ScanState.Invalid)
    }
}