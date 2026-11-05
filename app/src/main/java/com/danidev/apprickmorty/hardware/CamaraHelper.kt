package com.danidev.apprickmorty.hardware

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File

/** Paso 5: CameraX (Preview + ImageCapture) atado al ciclo de vida. */
class CamaraHelper(private val context: Context) {
    private var imageCapture: ImageCapture? = null

    fun iniciar(owner: LifecycleOwner, view: PreviewView) {
        val futuro = ProcessCameraProvider.getInstance(context)
        futuro.addListener({
            runCatching {
                val provider = futuro.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = view.surfaceProvider
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                imageCapture = capture
                provider.unbindAll()
                provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
            }.onFailure { Log.e("Camara", "No se pudo iniciar la cámara", it) }
        }, ContextCompat.getMainExecutor(context))
    }

    fun tomarFoto(onResultado: (File?) -> Unit) {
        val capture = imageCapture ?: return onResultado(null)
        val archivo = File(context.filesDir, "foto_${System.currentTimeMillis()}.jpg")
        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(archivo).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(r: ImageCapture.OutputFileResults) = onResultado(archivo)
                override fun onError(e: ImageCaptureException) {
                    Log.e("Camara", "Error al tomar foto", e)
                    onResultado(null)
                }
            }
        )
    }

    fun liberar() { imageCapture = null }
}
