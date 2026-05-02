package com.dam.gs.appentradas.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.gs.appentradas.data.repository.TicketRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: TicketRepositoryImpl
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.postValue(LoginState.Loading)
            try {
                android.util.Log.d("LOGIN", "Intentando autenticar: $username")
                repository.authenticate(username, password)
                android.util.Log.d("LOGIN", "Autenticación exitosa")
                _loginState.postValue(LoginState.Success)
            } catch (e: Exception) {
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