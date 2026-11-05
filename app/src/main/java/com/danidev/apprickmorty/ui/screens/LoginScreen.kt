package com.danidev.apprickmorty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danidev.apprickmorty.ui.theme.Background
import com.danidev.apprickmorty.ui.theme.Border
import com.danidev.apprickmorty.ui.theme.Primary
import com.danidev.apprickmorty.ui.theme.Surface
import com.danidev.apprickmorty.ui.theme.TextPrimary
import com.danidev.apprickmorty.ui.theme.TextSecondary
import com.danidev.apprickmorty.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onAutenticado: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val estado by viewModel.ui.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var esRegistro by rememberSaveable { mutableStateOf(false) }

    val campos = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
        focusedBorderColor = Primary, unfocusedBorderColor = Border,
        focusedLabelColor = Primary, unfocusedLabelColor = TextSecondary,
        cursorColor = Primary,
        focusedContainerColor = Surface, unfocusedContainerColor = Surface
    )

    Column(
        Modifier.fillMaxSize().background(Background).imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (esRegistro) "Crear cuenta" else "Iniciar sesión",
            color = TextPrimary, style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Rick & Morty", color = Primary, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        OutlinedTextField(
            value = email, onValueChange = { email = it; viewModel.limpiarError() },
            label = { Text("Correo") }, singleLine = true, colors = campos,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = clave, onValueChange = { clave = it; viewModel.limpiarError() },
            label = { Text("Contraseña") }, singleLine = true, colors = campos,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp))
        }

        Button(
            onClick = { viewModel.enviar(email, clave, esRegistro, onAutenticado) },
            enabled = !estado.cargando,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Background),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(52.dp)
        ) {
            if (estado.cargando) CircularProgressIndicator(Modifier.size(22.dp), color = Background, strokeWidth = 2.dp)
            else Text(if (esRegistro) "Registrarme" else "Entrar", fontWeight = FontWeight.SemiBold)
        }

        TextButton(onClick = { esRegistro = !esRegistro; viewModel.limpiarError() }, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                if (esRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate",
                color = TextSecondary
            )
        }
    }
}
