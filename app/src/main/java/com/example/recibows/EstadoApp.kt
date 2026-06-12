package com.example.recibows

import androidx.compose.runtime.mutableStateListOf
import com.example.recibows.componentes.LineaCarrito
import com.example.recibows.componentes.ProductoUI
import com.example.recibows.componentes.productosEjemplo
// Estado global compartido entre Venta, Carrito y Recibo.
// Cuando conectes Room, este objeto desaparece y lo reemplaza el ViewModel.
object EstadoApp {

    // ── Productos ──────────────────────────────────────────────────────────────
    val productos = mutableStateListOf<ProductoUI>().also {
        it.addAll(productosEjemplo)
    }

    fun agregarProducto(producto: ProductoUI) {
        val nuevoId = (productos.maxOfOrNull { it.id } ?: 0) + 1
        productos.add(producto.copy(id = nuevoId))
    }

    fun editarProducto(producto: ProductoUI) {
        val idx = productos.indexOfFirst { it.id == producto.id }
        if (idx >= 0) productos[idx] = producto
    }

    fun eliminarProducto(id: Int) {
        productos.removeIf { it.id == id }
    }

    // ── Carrito ────────────────────────────────────────────────────────────────
    val carrito = mutableStateListOf<LineaCarrito>()

    fun agregarAlCarrito(producto: ProductoUI) {
        val idx = carrito.indexOfFirst { it.producto.id == producto.id }
        if (idx >= 0) {
            carrito[idx] = carrito[idx].copy(cantidad = carrito[idx].cantidad + 1)
        } else {
            carrito.add(LineaCarrito(producto, 1))
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