package com.dam.gs.appentradas.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.gs.appentradas.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: TicketRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username.isEmpty() || password.isEmpty()) {
                _loginState.postValue(LoginState.Error("Rellena todos los campos"))
                return@launch
            }
            _loginState.postValue(LoginState.Loading)
            try {
                repository.authenticate(username, password)
                _loginState.postValue(LoginState.Success)
            }  catch (e: Exception) {
                val mensaje = when {
                    e.message == "credenciales_invalidas" -> "Usuario o contraseña incorrectos"
                    else -> "Error de conexión"
                }
                _loginState.postValue(LoginState.Error(mensaje))
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}