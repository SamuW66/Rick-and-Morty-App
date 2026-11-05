package com.danidev.apprickmorty.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.danidev.apprickmorty.data.auth.AuthRepository
import com.danidev.apprickmorty.data.cloudinary.CloudinaryUploader
import com.danidev.apprickmorty.data.cloudinary.ImagenesStore
import com.danidev.apprickmorty.hardware.AudioHelper
import com.danidev.apprickmorty.hardware.BleHelper
import com.danidev.apprickmorty.hardware.CamaraHelper
import com.danidev.apprickmorty.hardware.DispositivoBle
import com.danidev.apprickmorty.hardware.IrHelper
import com.danidev.apprickmorty.hardware.Permisos
import com.danidev.apprickmorty.hardware.SensoresHelper
import com.danidev.apprickmorty.ui.theme.Background
import com.danidev.apprickmorty.ui.theme.Border
import com.danidev.apprickmorty.ui.theme.Primary
import com.danidev.apprickmorty.ui.theme.Secondary
import com.danidev.apprickmorty.ui.theme.Surface
import com.danidev.apprickmorty.ui.theme.TextPrimary
import com.danidev.apprickmorty.ui.theme.TextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

// Patrón SOS en ms (ON, OFF, ...): ··· −−− ···
private val SOS: List<Long> = buildList {
    repeat(3) { add(200); add(200) }; add(400)
    repeat(3) { add(600); add(200) }; add(400)
    repeat(3) { add(200); add(200) }
}

@Composable
fun HardwareScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val camara = remember { CamaraHelper(context) }
    val audio = remember { AudioHelper(context) }
    val ble = remember { BleHelper(context) }
    val ir = remember { IrHelper(context) }
    val sensores = remember { SensoresHelper(context) }

    var foto by remember { mutableStateOf<File?>(null) }
    var subiendoFoto by remember { mutableStateOf(false) }
    val auth = remember { AuthRepository() }
    val imagenesStore = remember { ImagenesStore(context) }
    var camaraActiva by remember { mutableStateOf(false) }
    var grabando by remember { mutableStateOf(false) }
    var reproduciendo by remember { mutableStateOf(false) }
    var hayAudio by remember { mutableStateOf(false) }
    var escaneando by remember { mutableStateOf(false) }
    var sosActivo by remember { mutableStateOf(false) }
    var ax by remember { mutableFloatStateOf(0f) }
    var ay by remember { mutableFloatStateOf(0f) }
    var az by remember { mutableFloatStateOf(0f) }
    val dispositivos = remember { mutableStateListOf<DispositivoBle>() }
    var grabacionJob by remember { mutableStateOf<Job?>(null) }
    var sosJob by remember { mutableStateOf<Job?>(null) }

    fun toast(m: String) = Toast.makeText(context, m, Toast.LENGTH_SHORT).show()

    fun detenerGrabacion() {
        grabacionJob?.cancel(); grabacionJob = null
        if (grabando) { grabando = false; hayAudio = audio.detenerGrabacion() }
    }

    fun detenerSos() {
        sosJob?.cancel(); sosJob = null; sosActivo = false
        sensores.linterna(false); sensores.cancelarVibracion()
    }

    fun abrirAjustes() = context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    )

    // ---- Acciones por componente (se ejecutan cuando el permiso ya fue concedido) ----
    fun tomarFoto() {
        if (!camaraActiva) { camaraActiva = true; return }
        camara.tomarFoto { f -> if (f != null) { foto = f; toast("Foto guardada: ${f.name}") } else toast("Error al tomar foto") }
    }

    fun subirFoto(f: File) {
        val uid = auth.uid ?: return toast("Inicia sesión para subir imágenes")
        scope.launch {
            subiendoFoto = true
            CloudinaryUploader.subir(context, Uri.fromFile(f))
                .onSuccess { imagenesStore.agregar(uid, it); toast("Foto subida: mírala en Imágenes") }
                .onFailure { toast(it.message ?: "Error al subir la foto") }
            subiendoFoto = false
        }
    }

    fun grabar() {
        if (grabando) { detenerGrabacion(); return }
        if (!audio.iniciarGrabacion()) { toast("No se pudo iniciar la grabación"); return }
        grabando = true; hayAudio = false
        grabacionJob = scope.launch { delay(5_000); detenerGrabacion() } // 5 s
    }

    fun escanear() {
        if (!ble.disponible) { toast("Sin Bluetooth"); return }
        if (!ble.encendido) { toast("Activa el Bluetooth"); return }
        dispositivos.clear()
        escaneando = ble.escanear(
            onCambio = { dispositivos.clear(); dispositivos.addAll(it) },
            onFin = { escaneando = false }
        )
    }

    fun alternarSos() {
        if (sosActivo) { detenerSos(); return }
        if (!sensores.hayLinterna && !sensores.hayVibrador) { toast("Sin linterna ni vibrador"); return }
        sosActivo = true
        sosJob = scope.launch {
            try {
                while (true) {
                    SOS.forEachIndexed { i, ms ->
                        val on = i % 2 == 0
                        sensores.linterna(on)
                        if (on) sensores.vibrar(ms) else sensores.cancelarVibracion()
                        delay(ms)
                    }
                    sensores.linterna(false)
                    delay(1_400)
                }
            } finally {
                sensores.linterna(false); sensores.cancelarVibracion()
            }
        }
    }

    // ---- Permisos en tiempo de ejecución (Pasos 4 y flujo de la diapositiva 6) ----
    var pendiente by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pedidos by remember { mutableStateOf<Array<String>>(emptyArray()) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.all { it }) pendiente?.invoke()
        else {
            toast("Permiso denegado: función desactivada")
            // Si el sistema ya no muestra el diálogo, se ofrece abrir Ajustes.
            val activity = context as? android.app.Activity
            if (activity != null && pedidos.any { !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }) abrirAjustes()
        }
        pendiente = null
    }

    fun conPermiso(permisos: Array<String>, accion: () -> Unit) {
        if (Permisos.tieneTodos(context, permisos)) accion()
        else { pendiente = accion; pedidos = permisos; lanzador.launch(permisos) }
    }

    // ---- Ciclo de vida: acelerómetro mientras es visible y liberación en onStop() ----
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_START -> sensores.iniciarAcelerometro { x, y, z -> ax = x; ay = y; az = z }
                Lifecycle.Event.ON_STOP -> {
                    detenerGrabacion(); detenerSos()
                    audio.liberar(); reproduciendo = false
                    ble.detener(); sensores.liberar()
                    camaraActiva = false
                }
                else -> Unit
            }
        }
        lifecycle.lifecycle.addObserver(obs)
        onDispose {
            lifecycle.lifecycle.removeObserver(obs)
            detenerGrabacion(); detenerSos()
            audio.liberar(); ble.detener(); sensores.liberar(); camara.liberar()
        }
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
            }
            Text("Multisensor", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Tarjeta("Cámara") {
                    if (camaraActiva) {
                        AndroidView(
                            factory = { c -> PreviewView(c).also { camara.iniciar(lifecycle, it) } },
                            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp))
                        )
                    }
                    foto?.let {
                        AsyncImage(
                            model = it, contentDescription = "Foto tomada", contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                        )
                    }
                    foto?.let { f ->
                        Boton(if (subiendoFoto) "Subiendo…" else "Subir foto a Cloudinary", enabled = !subiendoFoto, secundario = true) { subirFoto(f) }
                    }
                    Boton(if (camaraActiva) "Tomar foto" else "Abrir cámara") { conPermiso(Permisos.camara, ::tomarFoto) }
                }
            }
            item {
                Tarjeta("Micrófono") {
                    Text(
                        when { grabando -> "Grabando (5 s)…"; reproduciendo -> "Reproduciendo…"; hayAudio -> "Audio listo"; else -> "Sin grabación" },
                        color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Boton(if (grabando) "Detener" else "Grabar 5 s", Modifier.weight(1f)) { conPermiso(Permisos.microfono, ::grabar) }
                        Boton("Reproducir", Modifier.weight(1f), enabled = hayAudio && !grabando, secundario = true) {
                            reproduciendo = audio.reproducir { reproduciendo = false }
                        }
                    }
                }
            }
            item {
                Tarjeta("Bluetooth (BLE)") {
                    Boton(if (escaneando) "Escaneando…" else "Buscar dispositivos", enabled = !escaneando) {
                        conPermiso(Permisos.bluetooth, ::escanear)
                    }
                }
            }
            items(dispositivos, key = { it.mac }) { d ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(12.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(d.nombre, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(d.mac, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    }
                    Text("${d.rssi} dBm", color = Secondary, style = MaterialTheme.typography.labelLarge)
                }
            }
            item {
                Tarjeta("Infrarrojo") {
                    Text(
                        if (ir.disponible) "Emisor IR disponible" else "Este dispositivo no tiene emisor IR",
                        color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                    )
                    Boton("Enviar señal IR", enabled = ir.disponible) {
                        toast(if (ir.enviar()) "Señal IR enviada" else "No se pudo enviar la señal")
                    }
                }
            }
            item {
                Tarjeta("Nivel de burbuja") {
                    Text("x=%.1f  y=%.1f  z=%.1f".format(ax, ay, az), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    if (!sensores.hayAcelerometro) Text("Sin acelerómetro", color = TextSecondary)
                    else Burbuja(ax, ay)
                }
            }
            item {
                Tarjeta("Linterna SOS + vibración") {
                    Boton(if (sosActivo) "Detener SOS" else "Iniciar SOS", enabled = sensores.hayLinterna || sensores.hayVibrador) {
                        alternarSos()
                    }
                }
            }
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface)
            .border(1.dp, Border, RoundedCornerShape(16.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(titulo, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        contenido()
    }
}

@Composable
private fun Boton(texto: String, modifier: Modifier = Modifier, enabled: Boolean = true, secundario: Boolean = false, onClick: () -> Unit) {
    if (secundario) {
        OutlinedButton(onClick, modifier.fillMaxWidth(), enabled = enabled, shape = RoundedCornerShape(12.dp)) {
            Text(texto, color = if (enabled) Primary else TextSecondary, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Button(
            onClick, modifier.fillMaxWidth(), enabled = enabled, shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Background)
        ) { Text(texto, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun Burbuja(x: Float, y: Float) {
    val nivelado = abs(x) < 0.5f && abs(y) < 0.5f
    val color = if (nivelado) Primary else Secondary
    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(170.dp)) {
            val r = size.minDimension / 2
            drawCircle(Border, r, style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
            drawCircle(Border, r / 4, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            // La burbuja se desplaza hacia el lado "alto" (signo invertido en x)
            val max = r * 0.8f
            val dx = (-x / 9.8f * max * 2).coerceIn(-max, max)
            val dy = (y / 9.8f * max * 2).coerceIn(-max, max)
            drawCircle(color, r / 6, center + Offset(dx, dy))
        }
    }
}
