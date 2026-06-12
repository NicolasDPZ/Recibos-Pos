package com.example.recibows.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

// ── Tabla de ventas ────────────────────────────────────────────────────────────
@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: Long = System.currentTimeMillis(),
    val total: Int,
    val metodoPago: String = "Efectivo"
)

// ── Tabla de items de cada venta ───────────────────────────────────────────────
@Entity(tableName = "items_venta")
data class ItemVentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ventaId: Int,           // referencia a VentaEntity
    val nombreProducto: String,
    val precioUnitario: Int,
    val cantidad: Int
) {
    val subtotal: Int get() = precioUnitario * cantidad
}

// ── Relación venta con sus items (para el recibo) ──────────────────────────────
data class VentaConItems(
    @Embedded val venta: VentaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ventaId"
    )
    val items: List<ItemVentaEntity>
)
