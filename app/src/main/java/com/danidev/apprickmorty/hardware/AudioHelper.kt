package com.danidev.apprickmorty.hardware

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/** Paso 6: MediaRecorder (MIC → MPEG_4/AAC) + MediaPlayer. */
class AudioHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    var archivo: File? = null
        private set

    val grabando get() = recorder != null

    @Suppress("DEPRECATION")
    fun iniciarGrabacion(): Boolean {
        detenerReproduccion()
        val destino = File(context.filesDir, "audio_${System.currentTimeMillis()}.m4a")
        return runCatching {
            recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
            else MediaRecorder()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(destino.absolutePath)
                prepare()
                start()
            }
            archivo = destino
            true
        }.getOrElse {
            Log.e("Audio", "Error al grabar", it)
            liberarRecorder(); false
        }
    }

    fun detenerGrabacion(): Boolean {
        val r = recorder ?: return false
        recorder = null
        return runCatching { r.stop(); true }
            .getOrElse { archivo?.delete(); archivo = null; false }
            .also { r.release() }
    }

    fun reproducir(onFin: () -> Unit): Boolean {
        val f = archivo?.takeIf { it.exists() } ?: return false
        detenerReproduccion()
        return runCatching {
            player = MediaPlayer().apply {
                setDataSource(f.absolutePath)
                prepare()
                setOnCompletionListener { detenerReproduccion(); onFin() }
                start()
            }
            true
        }.getOrElse { Log.e("Audio", "Error al reproducir", it); detenerReproduccion(); false }
    }

    fun detenerReproduccion() {
        player?.runCatching { release() }
        player = null
    }

    private fun liberarRecorder() {
        recorder?.runCatching { release() }
        recorder = null
    }

    /** Llamar en onStop(): libera micrófono y reproductor. */
    fun liberar() {
        if (recorder != null) detenerGrabacion()
        detenerReproduccion()
    }
}
