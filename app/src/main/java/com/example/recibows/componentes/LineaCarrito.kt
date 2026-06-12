package com.example.recibows.componentes

import com.example.recibows.componentes.ProductoUI

data class LineaCarrito(
    val producto: ProductoUI,
    val cantidad: Int
) {
    val subtotal: Int get() = producto.precio * cantidad
}