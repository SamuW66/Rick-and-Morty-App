package com.danidev.apprickmorty.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Envuelve FirebaseAuth: registro, inicio y cierre de sesión con correo y contraseña. */
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val estaLogueado: Boolean get() = auth.currentUser != null
    val uid: String? get() = auth.currentUser?.uid
    val email: String get() = auth.currentUser?.email.orEmpty()

    suspend fun registrar(email: String, clave: String): Result<Unit> =
        runCatching { auth.createUserWithEmailAndPassword(email, clave).await(); Unit }
            .recoverCatching { throw Exception(traducir(it)) }

    suspend fun iniciarSesion(email: String, clave: String): Result<Unit> =
        runCatching { auth.signInWithEmailAndPassword(email, clave).await(); Unit }
            .recoverCatching { throw Exception(traducir(it)) }

    fun cerrarSesion() = auth.signOut()

    private fun traducir(e: Throwable): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres"
        is FirebaseAuthUserCollisionException -> "Ese correo ya está registrado"
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseNetworkException -> "Sin conexión a internet"
        else -> e.localizedMessage ?: "Error desconocido"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { c ->
    addOnSuccessListener { c.resume(it) }
    addOnFailureListener { c.resumeWithException(it) }
}
