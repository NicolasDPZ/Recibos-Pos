package com.example.recibows.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ProductoRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).productoDao()

    // Flow que emite la lista cada vez que hay un cambio en la BD
    val productos: Flow<List<ProductoEntity>> = dao.getAll()

    suspend fun agregar(nombre: String, nombreCorto: String, precio: Int) {
        dao.insertar(
            ProductoEntity(
                nombre = nombre,
                nombreCorto = nombreCorto.ifBlank { nombre.take(8) },
                precio = precio
            )
        )
    }

    suspend fun editar(producto: ProductoEntity) {
        dao.actualizar(producto)
    }

    suspend fun eliminar(id: Int) {
        dao.eliminar(id)
    }
}
