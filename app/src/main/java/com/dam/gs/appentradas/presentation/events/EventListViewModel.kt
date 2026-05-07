package com.dam.gs.appentradas.presentation.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.gs.appentradas.domain.model.Event
import com.dam.gs.appentradas.domain.usecase.GetEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEvents: GetEvents
) : ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadEvents() {
        viewModelScope.launch {
            try {
                val result = getEvents()
                _events.postValue(result)
            } catch (e: Exception) {
                _error.postValue("No se pudieron cargar los eventos")
            }
        }
    }
}