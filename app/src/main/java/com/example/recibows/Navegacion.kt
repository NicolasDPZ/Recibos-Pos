package com.example.recibows

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recibows.pantallas.Carrito
import com.example.recibows.pantallas.Recibo
import com.example.recibows.pantallas.Venta

@Composable
fun Navegacion() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "venta"
    ) {
        composable(route = "venta") {
            Venta(navController = navController)
        }
        composable(route = "carrito") {
            Carrito(navController = navController)
        }
        composable(route = "recibo") {
            Recibo(navController = navController)
        }
    }
}