package com.example.recibows.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: Long = System.currentTimeMillis(),
    val total: Int,
    val metodoPago: String = "Efectivo",
    val atendienteId: Int = 0,
    val atendienteNombre: String = ""   // guardamos el nombre para no perderlo si se elimina el atendiente
)

@Entity(tableName = "items_venta")
data class ItemVentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ventaId: Int,
    val nombreProducto: String,
    val precioUnitario: Int,
    val cantidad: Int
) {
    val subtotal: Int get() = precioUnitario * cantidad
}

data class VentaConItems(
    @Embedded val venta: VentaEntity,
    @Relation(parentColumn = "id", entityColumn = "ventaId")
    val items: List<ItemVentaEntity>
)