package com.danidev.apprickmorty.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Permisos peligrosos agrupados por componente (según versión de Android). */
object Permisos {
    val camara = arrayOf(Manifest.permission.CAMERA)
    val microfono = arrayOf(Manifest.permission.RECORD_AUDIO)
    val bluetooth: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    val todos: Array<String> = (camara + microfono + bluetooth)

    fun tiene(context: Context, permiso: String) =
        ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED

    fun tieneTodos(context: Context, permisos: Array<String>) = permisos.all { tiene(context, it) }
}
