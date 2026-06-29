package com.example.recibows.pantallas

import android.app.Application
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recibows.data.AppDatabase
import com.example.recibows.data.VentaConItems
import com.example.recibows.ui.theme.PosBg
import com.example.recibows.ui.theme.PosBlue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── ViewModel del historial ────────────────────────────────────────────────────
class HistorialViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).ventaDao()

    val todasLasVentas: StateFlow<List<VentaConItems>> = dao.getTodasConItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ventasHoy: StateFlow<List<VentaConItems>> = dao.getVentasDesde(
        System.currentTimeMillis() / 86400000 * 86400000
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// ── Pantalla historial ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    navController: NavController,
    vm: HistorialViewModel = viewModel()
) {
    val todasLasVentas by vm.todasLasVentas.collectAsState()
    val ventasHoy      by vm.ventasHoy.collectAsState()

    // Tabs: Hoy / Todo / Por atendiente
    var tabSeleccionado by remember { mutableStateOf(0) }
    var atendienteFiltro by remember { mutableStateOf<String?>(null) }

    val ventasMostradas = when (tabSeleccionado) {
        0 -> ventasHoy
        1 -> todasLasVentas
        2 -> todasLasVentas.filter {
            atendienteFiltro == null || it.venta.atendienteNombre == atendienteFiltro
        }
        else -> ventasHoy
    }

    val totalMostrado = ventasMostradas.sumOf { it.venta.total }

    // Lista de atendientes únicos
    val atendientesUnicos = todasLasVentas.map { it.venta.atendienteNombre }.distinct().sorted()

    Scaffold(
        containerColor = PosBg,
        topBar = {
            TopAppBar(
                title = { Text("Historial de ventas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Resumen total ─────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PosBlue)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (tabSeleccionado) {
                                0 -> "Total vendido hoy"
                                1 -> "Total general"
                                else -> "Total — ${atendienteFiltro ?: "todos"}"
                            },
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            "\$${"%,d".format(totalMostrado).replace(',', '.')}",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${ventasMostradas.size} ventas", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(
                            "${ventasMostradas.sumOf { it.items.sumOf { i -> i.cantidad } }} items",
                            color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp
                        )
                    }
                }
            }

            // ── Tabs ──────────────────────────────────────────────────────────
            TabRow(selectedTabIndex = tabSeleccionado, containerColor = Color.White) {
                Tab(selected = tabSeleccionado == 0, onClick = { tabSeleccionado = 0 }) {
                    Text("Hoy", modifier = Modifier.padding(vertical = 12.dp), fontSize = 13.sp)
                }
                Tab(selected = tabSeleccionado == 1, onClick = { tabSeleccionado = 1 }) {
                    Text("Todo", modifier = Modifier.padding(vertical = 12.dp), fontSize = 13.sp)
                }
                Tab(selected = tabSeleccionado == 2, onClick = { tabSeleccionado = 2 }) {
                    Text("Atendiente", modifier = Modifier.padding(vertical = 12.dp), fontSize = 13.sp)
                }
            }

            // ── Selector de atendiente (tab 2) ────────────────────────────────
            if (tabSeleccionado == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = atendienteFiltro == null,
                        onClick = { atendienteFiltro = null },
                        label = { Text("Todos") }
                    )
                    atendientesUnicos.forEach { nombre ->
                        FilterChip(
                            selected = atendienteFiltro == nombre,
                            onClick = { atendienteFiltro = nombre },
                            label = { Text(nombre) }
                        )
                    }
                }
            }

            // ── Lista de ventas ───────────────────────────────────────────────
            if (ventasMostradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay ventas registradas", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ventasMostradas, key = { it.venta.id }) { ventaConItems ->
                        TarjetaVenta(ventaConItems = ventaConItems)
                    }
                }
            }
        }
    }
}

// ── Tarjeta de una venta en el historial ──────────────────────────────────────
@Composable
fun TarjetaVenta(ventaConItems: VentaConItems) {
    val venta = ventaConItems.venta
    val fecha = SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale("es", "CO")).format(Date(venta.fecha))
    val total = "%,d".format(venta.total).replace(',', '.')

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Recibo #${venta.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(fecha, fontSize = 11.sp, color = Color.Gray)
                }
                Text("\$$total", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PosBlue)
            }

            Spacer(Modifier.height(6.dp))

            // Atendiente
            if (venta.atendienteNombre.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(venta.atendienteNombre, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
            }

            Divider()
            Spacer(Modifier.height(4.dp))

            // Items
            ventaConItems.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.nombreProducto} x${item.cantidad}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(
                        "\$${"%,d".format(item.subtotal).replace(',', '.')}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
