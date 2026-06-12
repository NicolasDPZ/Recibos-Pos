package com.example.recibows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recibows.data.ProductoEntity
import com.example.recibows.data.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ProductoRepository(app)

    // Búsqueda
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    // Lista filtrada que la UI observa
    val productos: StateFlow<List<ProductoEntity>> = combine(
        repo.productos,
        _busqueda
    ) { lista, query ->
        if (query.isBlank()) lista
        else lista.filter { it.nombre.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onBusqueda(query: String) {
        _busqueda.value = query
    }

    fun agregar(nombre: String, nombreCorto: String, precio: Int) {
        viewModelScope.launch {
            repo.agregar(nombre, nombreCorto, precio)
        }
    }

    fun editar(producto: ProductoEntity) {
        viewModelScope.launch {
            repo.editar(producto)
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            repo.eliminar(id)
        }
    }
}
