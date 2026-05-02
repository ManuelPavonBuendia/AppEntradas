package com.dam.gs.appentradas.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dam.gs.appentradas.R
import com.dam.gs.appentradas.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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
        val prefs = requireActivity().getSharedPreferences("ajustes", 0)

        when (prefs.getString("idioma", "es")) {
            "es" -> binding.rgIdioma.check(R.id.rbEspanol)
            "en" -> binding.rgIdioma.check(R.id.rbIngles)
            else -> binding.rgIdioma.check(R.id.rbEspanol)
        }

        when (prefs.getInt("tema", AppCompatDelegate.MODE_NIGHT_NO)) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.rgTema.check(R.id.rbClaro)
            AppCompatDelegate.MODE_NIGHT_YES -> binding.rgTema.check(R.id.rbOscuro)
            else -> binding.rgTema.check(R.id.rbClaro)
        }
    }

    private fun configurarListeners() {
        val prefs = requireActivity().getSharedPreferences("ajustes", 0)

        binding.rgIdioma.setOnCheckedChangeListener { _, checkedId ->
            val idioma = when (checkedId) {
                R.id.rbEspanol -> "es"
                R.id.rbIngles -> "en"
                else -> "es"
            }
            prefs.edit().putString("idioma", idioma).apply()
            val localeList = LocaleListCompat.forLanguageTags(idioma)
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        binding.rgTema.setOnCheckedChangeListener { _, checkedId ->
            val modo = when (checkedId) {
                R.id.rbClaro -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbOscuro -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
            prefs.edit().putInt("tema", modo).apply()

            // Aplica el tema solo a esta Activity sin recrearla
            (requireActivity() as AppCompatActivity).delegate.localNightMode = modo
        }
        binding.btnCerrarSesion.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}