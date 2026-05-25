package com.dam.gs.appentradas.presentation.scanner

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.gs.appentradas.R
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.core.exceptions.TicketNotFoundException
import com.dam.gs.appentradas.domain.model.EstadoTicket
import com.dam.gs.appentradas.domain.model.Ticket
import com.dam.gs.appentradas.domain.usecase.CheckInTicket
import com.dam.gs.appentradas.domain.usecase.ValidateTicket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val validateTicket: ValidateTicket,
    private val checkInTicket: CheckInTicket
) : ViewModel() {

    private val _scanState = MutableLiveData<ScanState>()
    val scanState: LiveData<ScanState> = _scanState

    private var procesando = false

    fun handleScan(code: String, eventId: Int, eventName: String) {
        if (procesando) return
        procesando = true

        viewModelScope.launch {
            try {
                val ticket = validateTicket(code, eventId, eventName)
                if (ticket == null) {
                    _scanState.postValue(ScanState.Invalid)
                } else {
                    processTicket(ticket, code, eventId, eventName)
                }
            } catch (e: TicketNotFoundException) {
                _scanState.postValue(ScanState.Invalid)
            } catch (e: ConexionException) {
                _scanState.postValue(ScanState.Error(R.string.error_conexion))
            } catch (e: Exception) {
                _scanState.postValue(ScanState.Error(R.string.error_desconocido))
            }
            finally {
                kotlinx.coroutines.delay(5000)
                procesando = false
                _scanState.postValue(ScanState.Ready)
            }
        }
    }

    private suspend fun processTicket(ticket: Ticket, code: String, eventId: Int, eventName: String) {
        when (ticket.estado) {
            EstadoTicket.DONE -> _scanState.postValue(ScanState.AlreadyUsed)
            EstadoTicket.CANCELLED -> _scanState.postValue(ScanState.Invalid)
            else -> performCheckIn(ticket, code, eventId, eventName)
        }
    }

    private suspend fun performCheckIn(ticket: Ticket, code: String, eventId: Int, eventName: String) {
        try {
            checkInTicket(ticket.id, code)
            _scanState.postValue(ScanState.Valid(ticket.nombre, ticket.cliente))
        } catch (e: Exception) {
            val ticketActualizado = validateTicket(code, eventId, eventName)
            if (ticketActualizado?.estado == EstadoTicket.DONE) {
                _scanState.postValue(ScanState.Valid(ticket.nombre, ticket.cliente))
            } else {
                _scanState.postValue(ScanState.Error(R.string.error_checkin))
            }
        }
    }

    sealed class ScanState {
        object Ready : ScanState()
        object Invalid : ScanState()
        object AlreadyUsed : ScanState()
        data class Valid(val nombre: String, val cliente: String) : ScanState()
        data class Error(@StringRes val messageRes: Int) : ScanState()
    }
}