package com.example.recibows

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recibows.pantallas.Carrito
import com.example.recibows.pantallas.HistorialScreen
import com.example.recibows.pantallas.Recibo
import com.example.recibows.pantallas.Venta

@Composable
fun Navegacion() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "venta") {
        composable("venta") {
            Venta(navController = navController)
        }
        composable("carrito") {
            Carrito(navController = navController)
        }
        composable(
            route = "recibo/{ventaId}",
            arguments = listOf(navArgument("ventaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getInt("ventaId") ?: 0
            Recibo(navController = navController, ventaId = ventaId)
        }
        composable("historial") {
            HistorialScreen(navController = navController)
        }
    }
}