package com.example.recibows.data

import android.content.Context
import com.example.recibows.componentes.LineaCarrito
import kotlinx.coroutines.flow.Flow

class VentaRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).ventaDao()

    val ventas: Flow<List<VentaConItems>> = dao.getTodasConItems()

    suspend fun guardarVenta(
        lineas: List<LineaCarrito>,
        atendienteId: Int,
        atendienteNombre: String
    ): Int {
        val total = lineas.sumOf { it.subtotal }
        val venta = VentaEntity(
            total = total,
            atendienteId = atendienteId,
            atendienteNombre = atendienteNombre
        )
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

    suspend fun getVenta(ventaId: Int): VentaConItems? = dao.getVentaConItems(ventaId)

    // Ventas de hoy
    fun getVentasHoy(): Flow<List<VentaConItems>> {
        val inicioDia = System.currentTimeMillis() / 86400000 * 86400000
        return dao.getVentasDesde(inicioDia)
    }

    // Ventas por atendiente
    fun getVentasPorAtendiente(atendienteId: Int): Flow<List<VentaConItems>> =
        dao.getVentasPorAtendiente(atendienteId)
}