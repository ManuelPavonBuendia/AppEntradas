package com.dam.gs.appentradas.presentation.login

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.gs.appentradas.R
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.core.exceptions.CredencialesInvalidasException
import com.dam.gs.appentradas.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: TicketRepository) : ViewModel() {
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username.isEmpty() || password.isEmpty()) {
                _loginState.postValue(LoginState.Error(R.string.error_campos_vacios))
                return@launch
            }
            _loginState.postValue(LoginState.Loading)
            try {
                repository.authenticate(username, password)
                repository.descargarTodasLasEntradas { progreso ->
                    _loginState.postValue(LoginState.Descargando(progreso))
                }
                _loginState.postValue(LoginState.Success)
            } catch (e: CredencialesInvalidasException) {
                _loginState.postValue(LoginState.Error(R.string.error_credenciales))
            } catch (e: ConexionException) {
                _loginState.postValue(LoginState.Error(R.string.error_conexion))
            } catch (e: Exception) {
                _loginState.postValue(LoginState.Error(R.string.error_desconocido))
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Descargando(val progreso: Int) : LoginState()
    data class Error(@StringRes val messageRes: Int) : LoginState()
}