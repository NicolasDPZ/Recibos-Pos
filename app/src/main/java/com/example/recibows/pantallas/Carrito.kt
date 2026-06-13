package com.example.recibows.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recibows.CarritoViewModel
import com.example.recibows.componentes.LineaCarrito
import com.example.recibows.ui.theme.PosBg
import com.example.recibows.ui.theme.PosBlue
import com.example.recibows.ui.theme.PosRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Carrito(
    navController: NavController,
    vm: CarritoViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val lineas = vm.lineas
    val total  = vm.total

    Scaffold(
        containerColor = PosBg,
        topBar = {
            TopAppBar(
                title = { Text("Carrito", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal (${vm.items} items)",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "\$${"%,d".format(total).replace(',', '.')}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "\$${"%,d".format(total).replace(',', '.')}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PosBlue
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            // Guarda en Room y navega al recibo con el id
                            vm.cobrar { ventaId ->
                                navController.navigate("recibo/$ventaId") {
                                    popUpTo("carrito") { inclusive = true }
                                }
                            }
                        },
                        enabled = lineas.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PosBlue)
                    ) {
                        Text(
                            text = "Cobrar  \$${"%,d".format(total).replace(',', '.')}",
                            fontSize = 16.sp,
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
                        text = "Agrega productos desde la pantalla de venta",
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
}

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    text = "${linea.cantidad}",
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
                text = "\$$subtotal",
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