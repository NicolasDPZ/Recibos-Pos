package com.example.recibows.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    // Inserta la venta y retorna su id generado
    @Insert
    suspend fun insertarVenta(venta: VentaEntity): Long

    // Inserta todos los items de la venta
    @Insert
    suspend fun insertarItems(items: List<ItemVentaEntity>)

    // Trae todas las ventas con sus items (para historial)
    @Transaction
    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun getTodasConItems(): Flow<List<VentaConItems>>

    // Trae una venta específica con sus items (para el recibo)
    @Transaction
    @Query("SELECT * FROM ventas WHERE id = :ventaId")
    suspend fun getVentaConItems(ventaId: Int): VentaConItems?
}
