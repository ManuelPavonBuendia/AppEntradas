package com.dam.gs.appentradas.presentation.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dam.gs.appentradas.R
import com.dam.gs.appentradas.databinding.FragmentEventListBinding
import com.dam.gs.appentradas.domain.model.Event
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventListFragment : Fragment() {

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventListViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.loadEvents()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter { event -> onEventSelected(event) }
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onEventSelected(event: Event) {
        android.util.Log.d("CLICK", "Evento seleccionado: ${event.nombre}")
        val bundle = Bundle().apply {
            putInt("eventId", event.id)
            putString("eventName", event.nombre)
        }
        findNavController().navigate(R.id.action_events_to_scanner, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}