package com.example.recibows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recibows.data.VentaRepository
import kotlinx.coroutines.launch

class CarritoViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VentaRepository(app)

    // El carrito sigue viviendo en EstadoApp (en memoria)
    val lineas get() = EstadoApp.carrito
    val total   get() = EstadoApp.totalCarrito
    val items   get() = EstadoApp.itemsCarrito

    fun sumar(productoId: Int)   = EstadoApp.sumarCantidad(productoId)
    fun restar(productoId: Int)  = EstadoApp.restarCantidad(productoId)
    fun eliminar(productoId: Int) = EstadoApp.eliminarLinea(productoId)

    // Guarda la venta en Room y llama onListo con el id generado
    fun cobrar(onListo: (ventaId: Int) -> Unit) {
        val lineas = EstadoApp.carrito.toList()
        if (lineas.isEmpty()) return

        viewModelScope.launch {
            val ventaId = repo.guardarVenta(lineas)
            EstadoApp.limpiarCarrito()
            onListo(ventaId)
        }
    }
}
