package com.dam.gs.appentradas.presentation.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dam.gs.appentradas.R
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarPreferencias()
        binding.root.post { configurarListeners() }
    }

    private fun cargarPreferencias() {
        val prefs = requireActivity().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

        when (prefs.getString(AppConstants.PREF_IDIOMA, AppConstants.PREF_IDIOMA_DEFAULT)) {
            AppConstants.IDIOMA_ES -> binding.rgIdioma.check(R.id.rbEspanol)
            AppConstants.IDIOMA_EN -> binding.rgIdioma.check(R.id.rbIngles)
            else -> binding.rgIdioma.check(R.id.rbEspanol)
        }

        when (prefs.getInt(AppConstants.PREF_TEMA, AppCompatDelegate.MODE_NIGHT_NO)) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.rgTema.check(R.id.rbClaro)
            AppCompatDelegate.MODE_NIGHT_YES -> binding.rgTema.check(R.id.rbOscuro)
            else -> binding.rgTema.check(R.id.rbClaro)
        }
    }

    private fun configurarListeners() {
        val prefs = requireActivity().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

        binding.rgIdioma.setOnCheckedChangeListener { _, checkedId ->
            val idioma = when (checkedId) {
                R.id.rbEspanol -> AppConstants.IDIOMA_ES
                R.id.rbIngles -> AppConstants.IDIOMA_EN
                else -> AppConstants.PREF_IDIOMA_DEFAULT
            }
            prefs.edit().putString(AppConstants.PREF_IDIOMA, idioma).apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(idioma))
        }

        binding.rgTema.setOnCheckedChangeListener { _, checkedId ->
            val modo = when (checkedId) {
                R.id.rbClaro -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbOscuro -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
            prefs.edit().putInt(AppConstants.PREF_TEMA, modo).apply()
            (requireActivity() as AppCompatActivity).delegate.localNightMode = modo
        }

        binding.btnCerrarSesion.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}