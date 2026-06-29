package com.example.recibows.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    @Insert
    suspend fun insertarVenta(venta: VentaEntity): Long

    @Insert
    suspend fun insertarItems(items: List<ItemVentaEntity>)

    @Transaction
    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun getTodasConItems(): Flow<List<VentaConItems>>

    @Transaction
    @Query("SELECT * FROM ventas WHERE id = :ventaId")
    suspend fun getVentaConItems(ventaId: Int): VentaConItems?

    @Transaction
    @Query("SELECT * FROM ventas WHERE fecha >= :desde ORDER BY fecha DESC")
    fun getVentasDesde(desde: Long): Flow<List<VentaConItems>>

    @Transaction
    @Query("SELECT * FROM ventas WHERE atendienteId = :atendienteId ORDER BY fecha DESC")
    fun getVentasPorAtendiente(atendienteId: Int): Flow<List<VentaConItems>>
}