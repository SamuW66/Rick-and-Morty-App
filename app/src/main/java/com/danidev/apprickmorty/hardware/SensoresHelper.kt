package com.danidev.apprickmorty.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Paso 9: acelerómetro, linterna (sin permiso) y vibración. */
class SensoresHelper(private val context: Context) {
    private val sm = context.getSystemService(SensorManager::class.java)
    private val acelerometro: Sensor? = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val camManager = context.getSystemService(CameraManager::class.java)
    private val torchId: String? = runCatching {
        camManager?.cameraIdList?.firstOrNull {
            camManager.getCameraCharacteristics(it)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }.getOrNull()

    @Suppress("DEPRECATION")
    private val vibrador: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        else context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private var listener: SensorEventListener? = null

    val hayAcelerometro get() = acelerometro != null
    val hayLinterna get() = torchId != null
    val hayVibrador get() = vibrador?.hasVibrator() == true

    fun iniciarAcelerometro(onValores: (x: Float, y: Float, z: Float) -> Unit) {
        val s = acelerometro ?: return
        detenerAcelerometro()
        listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) = onValores(e.values[0], e.values[1], e.values[2])
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }.also { sm?.registerListener(it, s, SensorManager.SENSOR_DELAY_UI) }
    }

    fun detenerAcelerometro() {
        listener?.let { sm?.unregisterListener(it) }
        listener = null
    }

    fun linterna(encender: Boolean): Boolean {
        val id = torchId ?: return false
        return runCatching { camManager?.setTorchMode(id, encender); true }.getOrDefault(false)
    }

    fun vibrar(ms: Long = 300) {
        val v = vibrador?.takeIf { it.hasVibrator() } ?: return
        v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun cancelarVibracion() { vibrador?.cancel() }

    /** Llamar en onStop(): libera sensor, linterna y vibración. */
    fun liberar() {
        detenerAcelerometro()
        linterna(false)
        cancelarVibracion()
    }
}
