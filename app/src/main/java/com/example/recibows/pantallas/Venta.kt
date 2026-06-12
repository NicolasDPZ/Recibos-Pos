package com.example.recibows.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recibows.EstadoApp
import com.example.recibows.ProductoViewModel
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
    val totalItemsCarrito = EstadoApp.itemsCarrito

    var showDialog       by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<ProductoEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PosBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECIBO POS",
                    color = PosBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Atendiente", color = Color.Gray, fontSize = 13.sp)
                }
            }

            // ── Buscador ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = busqueda,
                onValueChange = { vm.onBusqueda(it) },
                placeholder = { Text("Buscar producto") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PosBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(52.dp)
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
                        nombreCorto = producto.nombreCorto,
                        precio = producto.precio,
                        cantidadEnCarrito = EstadoApp.carrito
                            .find { it.producto.id == producto.id }?.cantidad ?: 0,
                        onTap = {
                            EstadoApp.agregarAlCarrito(
                                com.example.recibows.componentes.ProductoUI(
                                    id = producto.id,
                                    nombre = producto.nombre,
                                    nombreCorto = producto.nombreCorto,
                                    precio = producto.precio
                                )
                            )
                        },
                        onLongPress = {
                            productoEditando = producto
                            showDialog = true
                        }
                    )
                }
            }

            // ── Botones inferiores ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) { Text("Pedidos y mesas", fontSize = 13.sp) }

                Button(
                    onClick = { navController.navigate("carrito") },
                    shape = RoundedCornerShape(24.dp),
                    enabled = totalItemsCarrito > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = PosBlue),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(
                        text = if (totalItemsCarrito > 0) "Ver carrito ($totalItemsCarrito)" else "Ver carrito",
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── FAB: nuevo producto ───────────────────────────────────────────────
        FloatingActionButton(
            onClick = {
                productoEditando = null
                showDialog = true
            },
            shape = CircleShape,
            containerColor = PosBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo producto")
        }
    }

    // ── Diálogo crear / editar ────────────────────────────────────────────────
    if (showDialog) {
        DialogProducto(
            producto = productoEditando,
            onGuardar = { nombre, nombreCorto, precio ->
                if (productoEditando == null) {
                    vm.agregar(nombre, nombreCorto, precio)
                } else {
                    vm.editar(
                        productoEditando!!.copy(
                            nombre = nombre,
                            nombreCorto = nombreCorto,
                            precio = precio
                        )
                    )
                }
                showDialog = false
            },
            onEliminar = { id ->
                vm.eliminar(id)
                showDialog = false
            },
            onCerrar = { showDialog = false }
        )
    }
}

// ── Tarjeta de producto ────────────────────────────────────────────────────────
@Composable
fun TarjetaProducto(
    nombre: String,
    nombreCorto: String,
    precio: Int,
    cantidadEnCarrito: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val precioFormateado = "%,d".format(precio).replace(',', '.')

    Card(
        onClick = onTap,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PosBlue),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (cantidadEnCarrito > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .align(Alignment.TopStart)
                        .offset(x = 6.dp, y = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$cantidadEnCarrito",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = nombreCorto,
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PosDarkBlue)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Column {
                        Text(
                            text = nombre,
                            color = Color(0xFF90CAF9),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "\$$precioFormateado",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Diálogo crear / editar producto ───────────────────────────────────────────
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
        title = {
            Text(
                text = if (esNuevo) "Nuevo producto" else "Editar producto",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        corto = it.take(8)
                        errNombre = false
                    },
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
                    onValueChange = {
                        precioStr = it.filter(Char::isDigit)
                        errPrecio = false
                    },
                    label = { Text("Precio ($)") },
                    singleLine = true,
                    isError = errPrecio,
                    supportingText = if (errPrecio) ({ Text("Ingresa un precio válido") }) else null,
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
                        onGuardar(
                            nombre.trim(),
                            corto.trim().ifBlank { nombre.take(8) },
                            precioStr.toInt()
                        )
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