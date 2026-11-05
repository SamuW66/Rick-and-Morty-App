package com.danidev.apprickmorty.data.cloudinary

import android.content.Context

/** Guarda en el teléfono las URLs de Cloudinary subidas por cada usuario. */
class ImagenesStore(context: Context) {
    private val prefs = context.getSharedPreferences("imagenes_cloudinary", Context.MODE_PRIVATE)

    private fun clave(uid: String) = "urls_$uid"

    fun listar(uid: String): List<String> =
        prefs.getString(clave(uid), "").orEmpty().split("\n").filter { it.isNotBlank() }

    fun agregar(uid: String, url: String) {
        prefs.edit().putString(clave(uid), (listOf(url) + listar(uid)).joinToString("\n")).apply()
    }
}
