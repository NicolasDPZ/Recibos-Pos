package com.example.recibows

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.recibows.componentes.LineaCarrito
import com.example.recibows.componentes.ProductoUI
import com.example.recibows.data.AtendienteEntity

object EstadoApp {

    // ── Atendiente seleccionado ────────────────────────────────────────────────
    var atendienteActual = mutableStateOf<AtendienteEntity?>(null)
    var impresoraSeleccionada: android.bluetooth.BluetoothDevice? = null

    fun seleccionarAtendiente(atendiente: AtendienteEntity) {
        atendienteActual.value = atendiente
    }

    // ── Carrito ────────────────────────────────────────────────────────────────
    val carrito = mutableStateListOf<LineaCarrito>()

    fun agregarAlCarrito(producto: ProductoUI, cantidad: Int = 1, precioOverride: Int? = null) {
        val precio = precioOverride ?: producto.precio
        val productoFinal = producto.copy(precio = precio)
        val idx = carrito.indexOfFirst { it.producto.id == productoFinal.id && it.producto.precio == precio }
        if (idx >= 0) {
            carrito[idx] = carrito[idx].copy(cantidad = carrito[idx].cantidad + cantidad)
        } else {
            carrito.add(LineaCarrito(productoFinal, cantidad))
        }
    }

    fun sumarCantidad(productoId: Int) {
        val idx = carrito.indexOfFirst { it.producto.id == productoId }
        if (idx >= 0) carrito[idx] = carrito[idx].copy(cantidad = carrito[idx].cantidad + 1)
    }

    fun restarCantidad(productoId: Int) {
        val idx = carrito.indexOfFirst { it.producto.id == productoId }
        if (idx < 0) return
        if (carrito[idx].cantidad <= 1) carrito.removeAt(idx)
        else carrito[idx] = carrito[idx].copy(cantidad = carrito[idx].cantidad - 1)
    }

    fun eliminarLinea(productoId: Int) {
        carrito.removeIf { it.producto.id == productoId }
    }

    fun limpiarCarrito() {
        carrito.clear()
    }

    val totalCarrito: Int get() = carrito.sumOf { it.subtotal }
    val itemsCarrito: Int get() = carrito.sumOf { it.cantidad }
}