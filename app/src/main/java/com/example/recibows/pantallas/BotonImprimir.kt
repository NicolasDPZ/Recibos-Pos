package com.example.recibows.pantallas

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recibows.EstadoApp
import com.example.recibows.EstadoImpresion
import com.example.recibows.ImpresionViewModel
import com.example.recibows.data.VentaConItems
import com.example.recibows.ui.theme.PosBlue
import com.example.recibows.ui.theme.PosRed

@SuppressLint("MissingPermission")
@Composable
fun BotonImprimir(
    venta: VentaConItems,
    nombreNegocio: String = "RECIBO POS",
    vm: ImpresionViewModel = viewModel()
) {
    val estado by vm.estado.collectAsState()
    var mostrarSelector by remember { mutableStateOf(false) }

    val impresoraActual = EstadoApp.impresoraSeleccionada
    val ocupado = estado == EstadoImpresion.Conectando || estado == EstadoImpresion.Imprimiendo

    Column(modifier = Modifier.fillMaxWidth()) {

        // Si ya hay impresora seleccionada muestra el nombre
        if (impresoraActual != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🖨️ ${impresoraActual.name}",
                    fontSize = 12.sp,
                    color = PosBlue,
                    fontWeight = FontWeight.SemiBold
                )
                // Botón para cambiar impresora
                TextButton(
                    onClick = { mostrarSelector = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Cambiar", fontSize = 11.sp)
                }
            }
        }

        // Botón principal de imprimir
        Button(
            onClick = {
                if (impresoraActual == null) {
                    // Primera vez: mostrar selector
                    mostrarSelector = true
                } else {
                    // Ya tiene impresora: imprimir directo
                    vm.resetEstado()
                    vm.imprimir(venta, nombreNegocio)
                }
            },
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PosBlue)
        ) {
            when (estado) {
                is EstadoImpresion.Conectando  -> {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Conectando...", fontSize = 15.sp)
                }
                is EstadoImpresion.Imprimiendo -> {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Imprimiendo...", fontSize = 15.sp)
                }
                is EstadoImpresion.Listo -> {
                    Text("✅ Impreso", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                else -> {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (impresoraActual == null) "Seleccionar impresora" else "Imprimir ticket",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Mensaje de error
        if (estado is EstadoImpresion.Error) {
            Text(
                text = (estado as EstadoImpresion.Error).mensaje,
                fontSize = 12.sp,
                color = PosRed,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    // Selector de impresora
    if (mostrarSelector) {
        val dispositivos = vm.dispositivosEmparejados
        AlertDialog(
            onDismissRequest = { mostrarSelector = false },
            title = { Text("Seleccionar impresora", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    if (dispositivos.isEmpty()) {
                        Text(
                            "No hay dispositivos Bluetooth emparejados.\n\nVe a Ajustes → Bluetooth y empareja la impresora PT210 primero.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        Text("Toca para seleccionar e imprimir:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        dispositivos.forEach { device ->
                            val esActual = EstadoApp.impresoraSeleccionada?.address == device.address
                            TextButton(
                                onClick = {
                                    // Guarda la impresora e imprime de una
                                    vm.seleccionarDispositivo(device)
                                    mostrarSelector = false
                                    vm.resetEstado()
                                    vm.imprimirEn(device, venta, nombreNegocio)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = device.name ?: "Dispositivo desconocido",
                                            fontWeight = if (esActual) FontWeight.Bold else FontWeight.Normal,
                                            color = if (esActual) PosBlue else Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(device.address, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    if (esActual) {
                                        Text("✓ Actual", fontSize = 11.sp, color = PosBlue)
                                    }
                                }
                            }
                            Divider()
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) { Text("Cancelar") }
            }
        )
    }
}