package com.example.recibows.data

import android.content.Context
import com.example.recibows.componentes.LineaCarrito
import kotlinx.coroutines.flow.Flow
import kotlin.collections.map

class VentaRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).ventaDao()

    val ventas: Flow<List<VentaConItems>> = dao.getTodasConItems()

    // Guarda la venta completa y retorna el id para el recibo
    suspend fun guardarVenta(lineas: List<LineaCarrito>): Int {
        val total = lineas.sumOf { it.subtotal }

        val venta = VentaEntity(total = total)
        val ventaId = dao.insertarVenta(venta).toInt()

        val items = lineas.map { linea ->
            ItemVentaEntity(
                ventaId        = ventaId,
                nombreProducto = linea.producto.nombre,
                precioUnitario = linea.producto.precio,
                cantidad       = linea.cantidad
            )
        }
        dao.insertarItems(items)

        return ventaId
    }

    suspend fun getVenta(ventaId: Int): VentaConItems? {
        return dao.getVentaConItems(ventaId)
    }
}
