package com.danidev.apprickmorty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danidev.apprickmorty.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.jvm.JvmOverloads

data class AuthUiState(val cargando: Boolean = false, val error: String? = null)

class AuthViewModel @JvmOverloads constructor(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun limpiarError() { _ui.value = _ui.value.copy(error = null) }

    fun enviar(email: String, clave: String, esRegistro: Boolean, onExito: () -> Unit) {
        val correo = email.trim()
        if (correo.isEmpty() || clave.isEmpty()) {
            _ui.value = AuthUiState(error = "Completa el correo y la contraseña"); return
        }
        if (esRegistro && clave.length < 6) {
            _ui.value = AuthUiState(error = "La contraseña debe tener al menos 6 caracteres"); return
        }
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            val r = if (esRegistro) repo.registrar(correo, clave) else repo.iniciarSesion(correo, clave)
            r.onSuccess { _ui.value = AuthUiState(); onExito() }
                .onFailure { _ui.value = AuthUiState(error = it.message) }
        }
    }
}
