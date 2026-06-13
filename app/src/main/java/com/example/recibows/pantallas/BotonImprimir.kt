package com.example.recibows.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recibows.EstadoImpresion
import com.example.recibows.ImpresionViewModel
import com.example.recibows.data.VentaConItems
import com.example.recibows.ui.theme.PosBlue
import com.example.recibows.ui.theme.PosRed

@Composable
fun BotonImprimir(
    venta: VentaConItems,
    nombreNegocio: String = "RECIBO POS",
    vm: ImpresionViewModel = viewModel()
) {
    val estado by vm.estado.collectAsState()
    var mostrarSelector by remember { mutableStateOf(false) }
    var mensajeEstado   by remember { mutableStateOf("") }

    // Actualiza el mensaje según el estado
    LaunchedEffect(estado) {
        mensajeEstado = when (estado) {
            is EstadoImpresion.Conectando  -> "Conectando con impresora..."
            is EstadoImpresion.Imprimiendo -> "Imprimiendo..."
            is EstadoImpresion.Listo       -> "✅ Impreso correctamente"
            is EstadoImpresion.Error       -> (estado as EstadoImpresion.Error).mensaje
            else -> ""
        }
    }

    // Botón principal
    Button(
        onClick = { mostrarSelector = true },
        enabled = estado == EstadoImpresion.Inactivo || estado == EstadoImpresion.Listo || estado is EstadoImpresion.Error,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PosBlue)
    ) {
        when (estado) {
            is EstadoImpresion.Conectando,
            is EstadoImpresion.Imprimiendo -> {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp))
            }
            else -> Text("🖨️  Imprimir ticket", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    // Mensaje de estado debajo del botón
    if (mensajeEstado.isNotBlank()) {
        Text(
            text = mensajeEstado,
            fontSize = 12.sp,
            color = if (estado is EstadoImpresion.Error) PosRed else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    // Diálogo selector de impresora
    if (mostrarSelector) {
        val dispositivos = vm.dispositivosEmparejados

        AlertDialog(
            onDismissRequest = { mostrarSelector = false },
            title = { Text("Seleccionar impresora", fontWeight = FontWeight.SemiBold) },
            text = {
                if (dispositivos.isEmpty()) {
                    Text(
                        "No hay dispositivos Bluetooth emparejados.\n\nVe a Ajustes → Bluetooth y empareja la impresora PT210 primero.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Dispositivos emparejados:", fontSize = 12.sp, color = Color.Gray)
                        Divider()
                        dispositivos.forEach { device ->
                            TextButton(
                                onClick = {
                                    mostrarSelector = false
                                    vm.resetEstado()
                                    vm.imprimir(device, venta, nombreNegocio)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = device.name ?: "Dispositivo desconocido",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = device.address,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
