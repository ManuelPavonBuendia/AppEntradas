package com.dam.gs.appentradas.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dam.gs.appentradas.R
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.databinding.FragmentLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0)

        // 1. LEER EL HISTORIAL: Recupera la lista de usuarios guardados
        val historialSet = prefs.getStringSet("historial_usuarios", emptySet()) ?: emptySet()
        val historialLista = historialSet.toList()

        // 2. MOSTRAR DESPLEGABLE: Si hay nombres, se los mete al buscador del campo de texto
        if (historialLista.isNotEmpty()) {
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                historialLista
            )
            binding.etUsuario.setAdapter(adapter)
        }

        binding.btnEntrar.setOnClickListener {
            val usuario = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(usuario, password)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> binding.btnEntrar.isEnabled = false
                is LoginState.Success -> {
                    // 3. GUARDAR EN HISTORIAL: Añade el usuario actual a la lista cuando entra bien
                    val usuarioActual = binding.etUsuario.text.toString().trim()
                    if (usuarioActual.isNotEmpty()) {
                        val actual = prefs.getStringSet("historial_usuarios", emptySet()) ?: emptySet()
                        val nuevoHistorial = actual.toMutableSet()
                        nuevoHistorial.add(usuarioActual)
                        prefs.edit().putStringSet("historial_usuarios", nuevoHistorial).apply()
                    }

                    findNavController().navigate(R.id.action_login_to_events)
                }
                is LoginState.Error -> {
                    binding.btnEntrar.isEnabled = true
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.login_error))
                        .setMessage(getString(state.messageRes))
                        .setPositiveButton(getString(android.R.string.ok)) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}