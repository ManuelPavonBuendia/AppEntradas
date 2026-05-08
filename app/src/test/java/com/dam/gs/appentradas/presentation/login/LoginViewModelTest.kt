package com.dam.gs.appentradas.presentation.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.core.exceptions.ConexionException
import com.dam.gs.appentradas.core.exceptions.CredencialesInvalidasException
import com.dam.gs.appentradas.domain.repository.TicketRepository
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
class LoginViewModelTest {
    companion object {
        const val USUARIO_TEST = "usuario"
        const val PASSWORD_TEST = "pass"
        const val PASSWORD_INCORRECTA = "mal"
    }


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: TicketRepository = mock()
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun camposVaciosError() = runTest {
        viewModel.login(AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING)
        assert(viewModel.loginState.value is LoginState.Error)
        assert((viewModel.loginState.value as LoginState.Error).message == AppConstants.ERROR_CAMPOS_VACIOS)
    }

    @Test
    fun credencialesInvalidasError() = runTest {
        whenever(repository.authenticate(USUARIO_TEST, PASSWORD_INCORRECTA))
            .thenThrow(CredencialesInvalidasException())
        viewModel.login(USUARIO_TEST, PASSWORD_INCORRECTA)
        assert((viewModel.loginState.value as LoginState.Error).message == AppConstants.ERROR_CREDENCIALES_MSG)
    }

    @Test
    fun conexionError() = runTest {
        whenever(repository.authenticate(USUARIO_TEST, PASSWORD_TEST))
            .thenThrow(ConexionException())
        viewModel.login(USUARIO_TEST, PASSWORD_TEST)
        assert((viewModel.loginState.value as LoginState.Error).message == AppConstants.ERROR_CONEXION)
    }

    @Test
    fun loginCorrecto() = runTest {
        viewModel.login(USUARIO_TEST, PASSWORD_TEST)
        assert(viewModel.loginState.value is LoginState.Success)
    }
}