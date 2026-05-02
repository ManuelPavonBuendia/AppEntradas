package com.dam.gs.appentradas.presentation.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam.gs.appentradas.databinding.ItemEventBinding
import com.dam.gs.appentradas.domain.model.Event

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<Event> = emptyList()

    fun submitList(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    inner class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.tvNombreEvento.text = event.nombre

            if (event.imagen != null) {
                val imageBytes = android.util.Base64.decode(event.imagen, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                android.util.Log.d("IMAGE", "bitmap null: ${bitmap == null}, bytes: ${imageBytes.size}")
                if (bitmap != null) {
                    binding.ivEvento.setImageBitmap(bitmap)
                } else {
                    binding.ivEvento.setImageResource(android.R.drawable.ic_menu_today)
                }
            }

            binding.root.setOnClickListener { onEventClick(event) }
        }
    }
}