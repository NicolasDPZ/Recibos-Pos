package com.example.recibows.pantallas

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recibows.ReciboViewModel
import com.example.recibows.data.ItemVentaEntity
import com.example.recibows.data.VentaConItems
import com.example.recibows.ui.theme.PosBg
import com.example.recibows.ui.theme.PosBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Recibo(
    navController: NavController,
    ventaId: Int,
    nombreNegocio: String = "RECIBO POS",
    vm: ReciboViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val venta by vm.venta.collectAsState()

    // Carga la venta desde Room cuando se abre la pantalla
    LaunchedEffect(ventaId) {
        vm.cargarVenta(ventaId)
    }

    Scaffold(
        containerColor = PosBg,
        topBar = {
            TopAppBar(
                title = { Text("Recibo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (venta != null) {
                        IconButton(onClick = {
                            compartirRecibo(context, nombreNegocio, venta!!)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->

        if (venta == null) {
            // Cargando...
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PosBlue)
            }
        } else {
            ContenidoRecibo(
                venta = venta!!,
                nombreNegocio = nombreNegocio,
                context = context,
                navController = navController,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ContenidoRecibo(
    venta: VentaConItems,
    nombreNegocio: String,
    context: Context,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val fecha = remember(venta.venta.fecha) {
        SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale("es", "CO"))
            .format(Date(venta.venta.fecha))
    }
    val numeroRecibo = "#${venta.venta.id}"
    val total = venta.venta.total

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Ticket ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nombreNegocio,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PosBlue,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(4.dp))
            Text("- - - - - - - - - - - - - - -", color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text("Recibo $numeroRecibo", fontSize = 13.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
            Text(fecha, fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFE0E0E0))
            Spacer(Modifier.height(8.dp))

            // Encabezado columnas
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Producto",  fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace)
                Text("Cant",      fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp), fontFamily = FontFamily.Monospace)
                Text("Subtotal",  fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.End, modifier = Modifier.width(80.dp), fontFamily = FontFamily.Monospace)
            }

            Spacer(Modifier.height(6.dp))
            Divider(color = Color(0xFFE0E0E0))
            Spacer(Modifier.height(6.dp))

            // Líneas
            venta.items.forEach { item ->
                FilaItem(item = item)
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = Color(0xFFE0E0E0))
            Spacer(Modifier.height(10.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TOTAL", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(
                    text = "\$${"%,d".format(total).replace(',', '.')}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PosBlue,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Método de pago", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                Text(venta.venta.metodoPago, fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
            }

            Spacer(Modifier.height(16.dp))
            Text("- - - - - - - - - - - - - - -", color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text("¡Gracias por su compra!", fontSize = 13.sp, color = Color.Gray, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(24.dp))

        // ── Botón WhatsApp ────────────────────────────────────────────────────
        Button(
            onClick = { compartirRecibo(context, nombreNegocio, venta) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Text("📲  Compartir por WhatsApp", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        BotonImprimir(venta = venta!!)

        Spacer(Modifier.height(10.dp))

        // ── Botón nueva venta ─────────────────────────────────────────────────
        OutlinedButton(
            onClick = {
                navController.navigate("venta") {
                    popUpTo("venta") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Nueva venta", fontSize = 15.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FilaItem(item: ItemVentaEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.nombreProducto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            Text(
                "\$${"%,d".format(item.precioUnitario).replace(',', '.')} c/u",
                fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace
            )
        }
        Text(
            "x${item.cantidad}",
            fontSize = 13.sp, textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp), fontFamily = FontFamily.Monospace
        )
        Text(
            "\$${"%,d".format(item.subtotal).replace(',', '.')}",
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(80.dp), fontFamily = FontFamily.Monospace
        )
    }
}

// ── Genera el texto y abre WhatsApp ───────────────────────────────────────────
fun compartirRecibo(
    context: Context,
    nombreNegocio: String,
    venta: VentaConItems
) {
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))
        .format(Date(venta.venta.fecha))

    val sb = StringBuilder()
    sb.appendLine("🧾 *$nombreNegocio*")
    sb.appendLine("Recibo #${venta.venta.id}")
    sb.appendLine("Fecha: $fecha")
    sb.appendLine("─────────────────")
    venta.items.forEach { item ->
        val sub = "%,d".format(item.subtotal).replace(',', '.')
        sb.appendLine("• ${item.nombreProducto} x${item.cantidad}  →  \$$sub")
    }
    sb.appendLine("─────────────────")
    sb.appendLine("*TOTAL: \$${"%,d".format(venta.venta.total).replace(',', '.')}*")
    sb.appendLine("Pago: ${venta.venta.metodoPago}")
    sb.appendLine("─────────────────")
    sb.appendLine("¡Gracias por su compra! 🙏")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(fallback, "Compartir recibo"))
    }
}