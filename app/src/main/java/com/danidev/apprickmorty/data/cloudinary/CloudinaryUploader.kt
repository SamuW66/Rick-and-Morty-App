package com.danidev.apprickmorty.data.cloudinary

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Datos de tu cuenta de Cloudinary.
 * - CLOUD_NAME: aparece en el Dashboard de Cloudinary.
 * - UPLOAD_PRESET: Settings > Upload > Upload presets > Add preset, con Signing mode = "Unsigned".
 * Nunca pongas aquí el API Secret: una app móvil se puede descompilar.
 */
object CloudinaryConfig {
    const val CLOUD_NAME = "y2ojvuyi"
    const val UPLOAD_PRESET = "apprickmorty"
    val configurado get() = !CLOUD_NAME.startsWith("TU_") && !UPLOAD_PRESET.startsWith("TU_")
}

object CloudinaryUploader {

    /** Sube la imagen (uri de galería o de archivo) y devuelve la URL https pública. */
    suspend fun subir(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            check(CloudinaryConfig.configurado) { "Configura CLOUD_NAME y UPLOAD_PRESET en CloudinaryConfig" }
            val limite = "----rm${System.currentTimeMillis()}"
            val conn = (URL("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/image/upload")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 60_000
                setChunkedStreamingMode(0)
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$limite")
            }
            try {
                DataOutputStream(conn.outputStream).use { out ->
                    out.writeBytes("--$limite\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                    out.writeBytes("${CloudinaryConfig.UPLOAD_PRESET}\r\n")
                    out.writeBytes("--$limite\r\nContent-Disposition: form-data; name=\"file\"; filename=\"imagen.jpg\"\r\n")
                    out.writeBytes("Content-Type: application/octet-stream\r\n\r\n")
                    val entrada = context.contentResolver.openInputStream(uri)
                        ?: error("No se pudo leer la imagen")
                    entrada.use { it.copyTo(out) }
                    out.writeBytes("\r\n--$limite--\r\n")
                }
                val ok = conn.responseCode in 200..299
                val cuerpo = (if (ok) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (!ok) {
                    val msg = runCatching { JSONObject(cuerpo).getJSONObject("error").getString("message") }.getOrNull()
                    error(msg ?: "Error ${conn.responseCode} al subir la imagen")
                }
                JSONObject(cuerpo).getString("secure_url")
            } finally {
                conn.disconnect()
            }
        }
    }

    /** URL optimizada por Cloudinary (formato y calidad automáticos, ancho máximo). */
    fun optimizada(url: String, ancho: Int = 600): String =
        url.replace("/upload/", "/upload/f_auto,q_auto,w_$ancho/")
}
