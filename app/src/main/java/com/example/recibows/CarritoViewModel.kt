package com.example.recibows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recibows.data.VentaRepository
import kotlinx.coroutines.launch

class CarritoViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VentaRepository(app)

    val lineas  get() = EstadoApp.carrito
    val total   get() = EstadoApp.totalCarrito
    val items   get() = EstadoApp.itemsCarrito
    val atendiente get() = EstadoApp.atendienteActual.value

    fun sumar(productoId: Int)    = EstadoApp.sumarCantidad(productoId)
    fun restar(productoId: Int)   = EstadoApp.restarCantidad(productoId)
    fun eliminar(productoId: Int) = EstadoApp.eliminarLinea(productoId)

    // Guarda la venta con el atendiente actual
    fun cobrar(onListo: (ventaId: Int) -> Unit) {
        val lineas = EstadoApp.carrito.toList()
        if (lineas.isEmpty()) return

        val atendiente = EstadoApp.atendienteActual.value

        viewModelScope.launch {
            val ventaId = repo.guardarVenta(
                lineas           = lineas,
                atendienteId     = atendiente?.id ?: 0,
                atendienteNombre = atendiente?.nombre ?: "Sin atendiente"
            )
            EstadoApp.limpiarCarrito()
            onListo(ventaId)
        }
    }
}