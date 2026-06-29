package com.example.recibows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recibows.data.AppDatabase
import com.example.recibows.data.AtendienteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AtendienteViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).atendienteDao()

    val atendientes: StateFlow<List<AtendienteEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregar(nombre: String) = viewModelScope.launch {
        if (nombre.isNotBlank()) dao.insertar(AtendienteEntity(nombre = nombre.trim()))
    }

    fun editar(atendiente: AtendienteEntity) = viewModelScope.launch {
        dao.actualizar(atendiente)
    }

    fun eliminar(id: Int) = viewModelScope.launch {
        dao.eliminar(id)
        // Si se elimina el atendiente actual, lo limpiamos
        if (EstadoApp.atendienteActual.value?.id == id) {
            EstadoApp.atendienteActual.value = null
        }
    }

    fun seleccionar(atendiente: AtendienteEntity) {
        EstadoApp.seleccionarAtendiente(atendiente)
    }
}
