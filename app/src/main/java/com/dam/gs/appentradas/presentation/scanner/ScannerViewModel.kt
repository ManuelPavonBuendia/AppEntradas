package com.dam.gs.appentradas.presentation.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                    when (ticket.estado) {
                        "done" -> _scanState.postValue(ScanState.AlreadyUsed)
                        else -> {
                            try {
                                checkInTicket(ticket.id)
                                _scanState.postValue(ScanState.Valid(ticket.nombre, ticket.cliente))
                            } catch (e: Exception) {
                                // Odoo puede fallar al responder pero haber procesado el ticket
                                // Verificamos el estado real
                                val ticketActualizado = validateTicket(code, eventId, eventName)
                                if (ticketActualizado?.estado == "done") {
                                    _scanState.postValue(
                                        ScanState.Valid(
                                            ticket.nombre,
                                            ticket.cliente
                                        )
                                    )
                                } else {
                                    _scanState.postValue(
                                        ScanState.Error(
                                            e.message ?: "Error desconocido"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _scanState.postValue(ScanState.Error(e.message ?: "Error desconocido"))
            } finally {
                kotlinx.coroutines.delay(5000)
                procesando = false
                _scanState.postValue(ScanState.Ready)
            }
        }
    }

    sealed class ScanState {
        object Ready : ScanState()
        object Invalid : ScanState()
        object AlreadyUsed : ScanState()
        data class Valid(val nombre: String, val cliente: String) : ScanState()
        data class Error(val message: String) : ScanState()
    }
}