package com.example.recibows.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Tabla atendientes ──────────────────────────────────────────────────────────
@Entity(tableName = "atendientes")
data class AtendienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)

// ── DAO ────────────────────────────────────────────────────────────────────────
@Dao
interface AtendienteDao {
    @Query("SELECT * FROM atendientes ORDER BY nombre ASC")
    fun getAll(): Flow<List<AtendienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(atendiente: AtendienteEntity)

    @Update
    suspend fun actualizar(atendiente: AtendienteEntity)

    @Query("DELETE FROM atendientes WHERE id = :id")
    suspend fun eliminar(id: Int)
}
