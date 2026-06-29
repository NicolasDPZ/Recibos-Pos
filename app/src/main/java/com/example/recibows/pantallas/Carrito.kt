package com.example.recibows.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recibows.AtendienteViewModel
import com.example.recibows.CarritoViewModel
import com.example.recibows.EstadoApp
import com.example.recibows.componentes.LineaCarrito
import com.example.recibows.ui.theme.PosBg
import com.example.recibows.ui.theme.PosBlue
import com.example.recibows.ui.theme.PosRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Carrito(
    navController: NavController,
    vm: CarritoViewModel = viewModel(),
    atendienteVm: AtendienteViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val lineas      = vm.lineas
    val total       = vm.total
    val atendiente  by EstadoApp.atendienteActual
    val atendientes by atendienteVm.atendientes.collectAsState()

    var showSelectorAtendiente by remember { mutableStateOf(false) }
    var showNuevoAtendiente    by remember { mutableStateOf(false) }
    var nuevoNombre            by remember { mutableStateOf("") }

    Scaffold(
        containerColor = PosBg,
        topBar = {
            TopAppBar(
                title = { Text("Carrito", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // ── Selector de atendiente ────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Atendiente:", fontSize = 13.sp, color = Color.Gray)
                        TextButton(onClick = { showSelectorAtendiente = true }) {
                            Text(
                                text = atendiente?.nombre ?: "Seleccionar ▾",
                                color = if (atendiente != null) PosBlue else PosRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Divider()
                    Spacer(Modifier.height(8.dp))

                    // ── Total ─────────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "\$${"%,d".format(total).replace(',', '.')}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PosBlue
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Botón cobrar ──────────────────────────────────────────
                    Button(
                        onClick = {
                            if (atendiente == null) {
                                showSelectorAtendiente = true
                            } else {
                                vm.cobrar { ventaId ->
                                    navController.navigate("recibo/$ventaId") {
                                        popUpTo("carrito") { inclusive = true }
                                    }
                                }
                            }
                        },
                        enabled = lineas.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PosBlue)
                    ) {
                        Text(
                            text = if (atendiente == null)
                                "Selecciona un atendiente primero"
                            else
                                "Cobrar  \$${"%,d".format(total).replace(',', '.')}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (lineas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("El carrito está vacío", fontSize = 16.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Agrega productos desde la pantalla de venta",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(lineas, key = { it.producto.id }) { linea ->
                    LineaCarritoItem(
                        linea      = linea,
                        onSumar    = { vm.sumar(linea.producto.id) },
                        onRestar   = { vm.restar(linea.producto.id) },
                        onEliminar = { vm.eliminar(linea.producto.id) }
                    )
                }
            }
        }
    }

    // ── Diálogo selector de atendiente ────────────────────────────────────────
    if (showSelectorAtendiente) {
        AlertDialog(
            onDismissRequest = { showSelectorAtendiente = false },
            title = { Text("Seleccionar atendiente", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    if (atendientes.isEmpty()) {
                        Text(
                            "No hay atendientes creados.\nCrea uno con el botón de abajo.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    } else {
                        atendientes.forEach { a ->
                            TextButton(
                                onClick = {
                                    atendienteVm.seleccionar(a)
                                    showSelectorAtendiente = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        a.nombre,
                                        fontSize = 15.sp,
                                        color = if (atendiente?.id == a.id) PosBlue else Color.Black
                                    )
                                    if (atendiente?.id == a.id) {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            tint = PosBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Divider()
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showNuevoAtendiente = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("+ Nuevo atendiente") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectorAtendiente = false }) { Text("Cerrar") }
            }
        )
    }

    // ── Diálogo crear nuevo atendiente ────────────────────────────────────────
    if (showNuevoAtendiente) {
        AlertDialog(
            onDismissRequest = { showNuevoAtendiente = false },
            title = { Text("Nuevo atendiente", fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoNombre.isNotBlank()) {
                            atendienteVm.agregar(nuevoNombre)
                            nuevoNombre = ""
                            showNuevoAtendiente = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PosBlue)
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showNuevoAtendiente = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Fila de item en el carrito ─────────────────────────────────────────────────
@Composable
fun LineaCarritoItem(
    linea: LineaCarrito,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onEliminar: () -> Unit
) {
    val precioUnit = "%,d".format(linea.producto.precio).replace(',', '.')
    val subtotal   = "%,d".format(linea.subtotal).replace(',', '.')

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(linea.producto.nombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("\$$precioUnit c/u", fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRestar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, null, Modifier.size(16.dp), tint = PosBlue)
                }
                Text(
                    "${linea.cantidad}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onSumar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = PosBlue)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "\$$subtotal",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PosBlue,
                modifier = Modifier.width(72.dp),
                textAlign = TextAlign.End
            )
            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = PosRed)
            }
        }
    }
}