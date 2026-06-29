package com.example.recibows

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recibows.data.VentaConItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

sealed class EstadoImpresion {
    object Inactivo    : EstadoImpresion()
    object Conectando  : EstadoImpresion()
    object Imprimiendo : EstadoImpresion()
    object Listo       : EstadoImpresion()
    data class Error(val mensaje: String) : EstadoImpresion()
}

@SuppressLint("MissingPermission")
class ImpresionViewModel(app: Application) : AndroidViewModel(app) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private val _estado = MutableStateFlow<EstadoImpresion>(EstadoImpresion.Inactivo)
    val estado: StateFlow<EstadoImpresion> = _estado

    // Dispositivo actualmente seleccionado (guardado en EstadoApp)
    val dispositivoActual: BluetoothDevice? get() = EstadoApp.impresoraSeleccionada

    val dispositivosEmparejados: List<BluetoothDevice>
        get() = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()

    // Guarda el dispositivo seleccionado en EstadoApp
    fun seleccionarDispositivo(device: BluetoothDevice) {
        EstadoApp.impresoraSeleccionada = device
    }

    // Imprime usando el dispositivo guardado
    fun imprimir(
        venta: VentaConItems,
        nombreNegocio: String = "RECIBO POS"
    ) {
        val device = EstadoApp.impresoraSeleccionada ?: return
        imprimirEn(device, venta, nombreNegocio)
    }

    // Imprime en un dispositivo específico (para cuando se selecciona por primera vez)
    fun imprimirEn(
        device: BluetoothDevice,
        venta: VentaConItems,
        nombreNegocio: String = "RECIBO POS"
    ) {
        viewModelScope.launch {
            _estado.value = EstadoImpresion.Conectando
            withContext(Dispatchers.IO) {
                var socket: BluetoothSocket? = null
                try {
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                    bluetoothAdapter?.cancelDiscovery()
                    socket.connect()
                    _estado.value = EstadoImpresion.Imprimiendo
                    val out = socket.outputStream
                    imprimirRecibo(out, venta, nombreNegocio)
                    out.flush()
                    _estado.value = EstadoImpresion.Listo
                } catch (e: Exception) {
                    _estado.value = EstadoImpresion.Error("Error: ${e.message}")
                } finally {
                    try { socket?.close() } catch (_: Exception) {}
                }
            }
        }
    }

    fun resetEstado() { _estado.value = EstadoImpresion.Inactivo }

    private fun imprimirRecibo(out: OutputStream, venta: VentaConItems, nombreNegocio: String) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))
            .format(Date(venta.venta.fecha))

        out.write(ESC_INIT)
        out.write(ALIGN_CENTER)
        out.write(BOLD_ON)
        out.write(SIZE_LARGE)
        out.write(textLine(nombreNegocio))
        out.write(SIZE_NORMAL)
        out.write(BOLD_OFF)
        out.write(textLine("--------------------------------"))
        out.write(textLine("Recibo #${venta.venta.id}"))
        out.write(textLine(fecha))
        if (venta.venta.atendienteNombre.isNotBlank()) {
            out.write(textLine("Atendiente: ${venta.venta.atendienteNombre}"))
        }
        out.write(textLine("--------------------------------"))
        out.write(ALIGN_LEFT)
        out.write(textLine(formatearLinea("Producto", "Cant", "Total", 32)))
        out.write(textLine("--------------------------------"))

        venta.items.forEach { item ->
            val subtotal = "\$${"%,d".format(item.subtotal).replace(',', '.')}"
            val cant = "x${item.cantidad}"
            out.write(textLine(formatearLinea(item.nombreProducto, cant, subtotal, 32)))
            val pu = "  \$${"%,d".format(item.precioUnitario).replace(',', '.')} c/u"
            out.write(textLine(pu))
        }

        out.write(textLine("--------------------------------"))
        out.write(BOLD_ON)
        val totalStr = "\$${"%,d".format(venta.venta.total).replace(',', '.')}"
        out.write(textLine(formatearLinea("TOTAL", "", totalStr, 32)))
        out.write(BOLD_OFF)
        out.write(textLine("Pago: ${venta.venta.metodoPago}"))
        out.write(textLine("--------------------------------"))
        out.write(ALIGN_CENTER)
        out.write(textLine("Gracias por su compra!"))
        out.write(textLine(""))
        out.write(FEED_LINES)
        out.write(CUT_PAPER)
    }

    private fun textLine(text: String): ByteArray = (text + "\n").toByteArray(Charsets.UTF_8)

    private fun formatearLinea(izq: String, centro: String, der: String, ancho: Int): String {
        val espacioDer = der.length
        val espacioCentro = centro.length
        val espacioIzq = ancho - espacioDer - espacioCentro
        val izqRecortado = if (izq.length > espacioIzq - 1) izq.take(espacioIzq - 1) else izq
        val padding = " ".repeat((espacioIzq - izqRecortado.length).coerceAtLeast(0))
        return "$izqRecortado$padding$centro$der"
    }

    companion object {
        val ESC_INIT     = byteArrayOf(0x1B, 0x40)
        val ALIGN_LEFT   = byteArrayOf(0x1B, 0x61, 0x00)
        val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        val BOLD_ON      = byteArrayOf(0x1B, 0x45, 0x01)
        val BOLD_OFF     = byteArrayOf(0x1B, 0x45, 0x00)
        val SIZE_LARGE   = byteArrayOf(0x1D, 0x21, 0x11)
        val SIZE_NORMAL  = byteArrayOf(0x1D, 0x21, 0x00)
        val FEED_LINES   = byteArrayOf(0x1B, 0x64, 0x03)
        val CUT_PAPER    = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
    }
}