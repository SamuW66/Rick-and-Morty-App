package com.danidev.apprickmorty.hardware

import android.content.Context
import android.hardware.ConsumerIrManager

/** Paso 8: emisor infrarrojo (patrón NEC a 38 kHz). */
class IrHelper(context: Context) {
    private val ir = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    val disponible get() = ir?.hasIrEmitter() == true

    // Encabezado 9 ms ON + 4,5 ms OFF, bits (560 µs + 560/1690 µs) y pulso final
    private val patron = intArrayOf(9000, 4500, 560, 560, 560, 1690, 560, 560, 560, 1690, 560)

    fun enviar(): Boolean {
        val m = ir?.takeIf { it.hasIrEmitter() } ?: return false
        return runCatching { m.transmit(38_000, patron); true }.getOrDefault(false)
    }
}
