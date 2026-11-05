package com.danidev.apprickmorty.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper

data class DispositivoBle(val nombre: String, val mac: String, val rssi: Int)

/** Paso 7: escaneo BLE de 10 s con resultados únicos por MAC. */
@SuppressLint("MissingPermission") // los permisos se verifican antes de llamar
class BleHelper(context: Context) {
    val adapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter

    private val handler = Handler(Looper.getMainLooper())
    private val encontrados = LinkedHashMap<String, DispositivoBle>()
    private var onCambio: ((List<DispositivoBle>) -> Unit)? = null
    private var onFin: (() -> Unit)? = null
    private var escaneando = false

    private val detener = Runnable { detener() }

    private val callback = object : ScanCallback() {
        override fun onScanResult(tipo: Int, r: ScanResult) {
            val d = DispositivoBle(r.device.name ?: "Desconocido", r.device.address, r.rssi)
            encontrados[d.mac] = d
            onCambio?.invoke(encontrados.values.sortedByDescending { it.rssi })
        }
    }

    val disponible get() = adapter != null
    val encendido get() = adapter?.isEnabled == true

    fun escanear(onCambio: (List<DispositivoBle>) -> Unit, onFin: () -> Unit): Boolean {
        val scanner = adapter?.takeIf { it.isEnabled }?.bluetoothLeScanner ?: return false
        if (escaneando) return true
        encontrados.clear()
        this.onCambio = onCambio
        this.onFin = onFin
        escaneando = true
        scanner.startScan(callback)
        handler.postDelayed(detener, 10_000)
        return true
    }

    fun detener() {
        if (!escaneando) return
        escaneando = false
        handler.removeCallbacks(detener)
        runCatching { adapter?.bluetoothLeScanner?.stopScan(callback) }
        onFin?.invoke()
        onCambio = null; onFin = null
    }
}
