package com.example.recibows.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recibows.EstadoApp
import com.example.recibows.ProductoViewModel
import com.example.recibows.componentes.ProductoUI
import com.example.recibows.data.ProductoEntity
import com.example.recibows.ui.theme.PosBg
import com.example.recibows.ui.theme.PosBlue
import com.example.recibows.ui.theme.PosDarkBlue
import com.example.recibows.ui.theme.PosRed

@Composable
fun Venta(
    navController: NavController,
    vm: ProductoViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val productos by vm.productos.collectAsState()
    val busqueda  by vm.busqueda.collectAsState()
    val atendiente by EstadoApp.atendienteActual
    val totalItemsCarrito = EstadoApp.itemsCarrito

    var showDialogProducto  by remember { mutableStateOf(false) }
    var showDialogAjuste    by remember { mutableStateOf(false) }
    var productoEditando    by remember { mutableStateOf<ProductoEntity?>(null) }
    var productoAjustando   by remember { mutableStateOf<ProductoEntity?>(null) }

    Box(modifier = modifier.fillMaxSize().background(PosBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RECIBO POS", color = PosBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = atendiente?.nombre ?: "Sin atendiente",
                        color = if (atendiente != null) PosBlue else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (atendiente != null) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // ── Buscador ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = busqueda,
                onValueChange = { vm.onBusqueda(it) },
                placeholder = { Text("Buscar producto") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PosBlue
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(52.dp)
            )

            Spacer(Modifier.height(8.dp))

            // ── Grid de productos ─────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(productos, key = { it.id }) { producto ->
                    TarjetaProducto(
                        nombre = producto.nombre,
                        precio = producto.precio,
                        cantidadEnCarrito = EstadoApp.carrito.find { it.producto.id == producto.id }?.cantidad ?: 0,
                        onTap = {
                            // Tap simple: agrega directo con precio base y cantidad 1
                            EstadoApp.agregarAlCarrito(
                                ProductoUI(
                                    id = producto.id,
                                    nombre = producto.nombre,
                                    nombreCorto = producto.nombreCorto,
                                    precio = producto.precio
                                )
                            )
                        },
                        onLongPress = {
                            // Long press: abre diálogo para ajustar precio/cantidad
                            productoAjustando = producto
                            showDialogAjuste = true
                        },
                        onDoubleTap = {
                            // Doble tap: editar el producto
                            productoEditando = producto
                            showDialogProducto = true
                        }
                    )
                }
            }

            // ── Botones inferiores ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("historial") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) { Text("Historial", fontSize = 13.sp) }

                Button(
                    onClick = { navController.navigate("carrito") },
                    shape = RoundedCornerShape(24.dp),
                    enabled = totalItemsCarrito > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = PosBlue),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(
                        text = if (totalItemsCarrito > 0) "Carrito ($totalItemsCarrito)" else "Ver carrito",
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── FAB nuevo producto ────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { productoEditando = null; showDialogProducto = true },
            shape = CircleShape,
            containerColor = PosBlue,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 80.dp)
        ) { Icon(Icons.Default.Add, "Nuevo producto") }
    }

    // ── Diálogo crear/editar producto (FAB o doble tap) ───────────────────────
    if (showDialogProducto) {
        DialogProducto(
            producto = productoEditando,
            onGuardar = { nombre, nombreCorto, precio ->
                if (productoEditando == null) vm.agregar(nombre, nombreCorto, precio)
                else vm.editar(productoEditando!!.copy(nombre = nombre, nombreCorto = nombreCorto, precio = precio))
                showDialogProducto = false
            },
            onEliminar = { id -> vm.eliminar(id); showDialogProducto = false },
            onCerrar = { showDialogProducto = false }
        )
    }

    // ── Diálogo ajustar precio/cantidad (long press) ──────────────────────────
    if (showDialogAjuste && productoAjustando != null) {
        DialogAjusteCarrito(
            producto = productoAjustando!!,
            onAgregar = { cantidad, precio ->
                EstadoApp.agregarAlCarrito(
                    ProductoUI(
                        id = productoAjustando!!.id,
                        nombre = productoAjustando!!.nombre,
                        nombreCorto = productoAjustando!!.nombreCorto,
                        precio = precio
                    ),
                    cantidad = cantidad,
                    precioOverride = precio
                )
                showDialogAjuste = false
            },
            onCerrar = { showDialogAjuste = false }
        )
    }
}

// ── Tarjeta con tap, long press y doble tap ───────────────────────────────────
@Composable
fun TarjetaProducto(
    nombre: String,
    precio: Int,
    cantidadEnCarrito: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDoubleTap: () -> Unit
) {
    val precioFormateado = "%,d".format(precio).replace(',', '.')

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(10.dp))
            .background(PosBlue)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() },
                    onDoubleTap = { onDoubleTap() }
                )
            }
    ) {
        // Badge cantidad
        if (cantidadEnCarrito > 0) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .align(Alignment.TopStart)
                    .offset(x = 6.dp, y = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$cantidadEnCarrito", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Nombre del producto
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = nombre,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }
            // Precio
            Box(
                modifier = Modifier.fillMaxWidth().background(PosDarkBlue).padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text("\$$precioFormateado", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Diálogo ajuste de precio y cantidad (solo para long press) ────────────────
@Composable
fun DialogAjusteCarrito(
    producto: ProductoEntity,
    onAgregar: (cantidad: Int, precio: Int) -> Unit,
    onCerrar: () -> Unit
) {
    var cantidadStr by remember { mutableStateOf("1") }
    var precioStr   by remember { mutableStateOf(producto.precio.toString()) }
    var errCantidad by remember { mutableStateOf(false) }
    var errPrecio   by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(producto.nombre, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Precio base: \$${"%,d".format(producto.precio).replace(',', '.')}",
                    fontSize = 12.sp, color = Color.Gray
                )
                OutlinedTextField(
                    value = cantidadStr,
                    onValueChange = { cantidadStr = it.filter(Char::isDigit); errCantidad = false },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    isError = errCantidad,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = precioStr,
                    onValueChange = { precioStr = it.filter(Char::isDigit); errPrecio = false },
                    label = { Text("Precio unitario (\$)") },
                    singleLine = true,
                    isError = errPrecio,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                val cantidad = cantidadStr.toIntOrNull() ?: 0
                val precio   = precioStr.toIntOrNull() ?: 0
                if (cantidad > 0 && precio > 0) {
                    Text(
                        "Subtotal: \$${"%,d".format(cantidad * precio).replace(',', '.')}",
                        fontWeight = FontWeight.Bold, color = PosBlue, fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errCantidad = cantidadStr.isBlank() || (cantidadStr.toIntOrNull() ?: 0) <= 0
                    errPrecio   = precioStr.isBlank() || (precioStr.toIntOrNull() ?: 0) <= 0
                    if (!errCantidad && !errPrecio) onAgregar(cantidadStr.toInt(), precioStr.toInt())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PosBlue)
            ) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

// ── Diálogo crear/editar producto ─────────────────────────────────────────────
@Composable
fun DialogProducto(
    producto: ProductoEntity?,
    onGuardar: (nombre: String, nombreCorto: String, precio: Int) -> Unit,
    onEliminar: (Int) -> Unit,
    onCerrar: () -> Unit
) {
    val esNuevo   = producto == null
    var nombre    by remember { mutableStateOf(producto?.nombre ?: "") }
    var corto     by remember { mutableStateOf(producto?.nombreCorto ?: "") }
    var precioStr by remember { mutableStateOf(producto?.precio?.toString() ?: "") }
    var errNombre by remember { mutableStateOf(false) }
    var errPrecio by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (esNuevo) "Nuevo producto" else "Editar producto", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; corto = it.take(8); errNombre = false },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = errNombre,
                    supportingText = if (errNombre) ({ Text("Campo requerido") }) else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = corto,
                    onValueChange = { if (it.length <= 8) corto = it },
                    label = { Text("Nombre corto (máx 8)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = precioStr,
                    onValueChange = { precioStr = it.filter(Char::isDigit); errPrecio = false },
                    label = { Text("Precio ($)") },
                    singleLine = true,
                    isError = errPrecio,
                    supportingText = if (errPrecio) ({ Text("Ingresa un precio válido") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errNombre = nombre.isBlank()
                    errPrecio = precioStr.isBlank()
                    if (!errNombre && !errPrecio) {
                        onGuardar(nombre.trim(), corto.trim().ifBlank { nombre.take(8) }, precioStr.toInt())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PosBlue)
            ) { Text(if (esNuevo) "Crear" else "Guardar") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!esNuevo && producto != null) {
                    TextButton(
                        onClick = { onEliminar(producto.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = PosRed)
                    ) { Text("Eliminar") }
                }
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        }
    )
}