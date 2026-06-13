package com.example.recibows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recibows.data.VentaConItems
import com.example.recibows.data.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReciboViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VentaRepository(app)

    private val _venta = MutableStateFlow<VentaConItems?>(null)
    val venta: StateFlow<VentaConItems?> = _venta

    fun cargarVenta(ventaId: Int) {
        viewModelScope.launch {
            _venta.value = repo.getVenta(ventaId)
        }
    }
}
