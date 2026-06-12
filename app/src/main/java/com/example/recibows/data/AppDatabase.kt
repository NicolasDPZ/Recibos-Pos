package com.example.recibows.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductoEntity::class,
        VentaEntity::class,
        ItemVentaEntity::class
    ],
    version = 2,                // subimos a 2 porque agregamos tablas
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recibows_db"
                )
                    .fallbackToDestructiveMigration() // borra y recrea si cambia la versión
                    .build().also { INSTANCE = it }
            }
        }
    }
}