package com.danidev.apprickmorty.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.danidev.apprickmorty.data.cloudinary.CloudinaryConfig
import com.danidev.apprickmorty.data.cloudinary.CloudinaryUploader
import com.danidev.apprickmorty.data.cloudinary.ImagenesStore
import com.danidev.apprickmorty.ui.theme.Background
import com.danidev.apprickmorty.ui.theme.Border
import com.danidev.apprickmorty.ui.theme.Primary
import com.danidev.apprickmorty.ui.theme.Surface
import com.danidev.apprickmorty.ui.theme.TextPrimary
import com.danidev.apprickmorty.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** Elige una imagen de la galería, la sube a Cloudinary y muestra todas las del usuario. */
@Composable
fun ImagenesScreen(uid: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { ImagenesStore(context) }
    val urls = remember { mutableStateListOf<String>().apply { addAll(store.listar(uid)) } }
    var subiendo by remember { mutableStateOf(false) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            subiendo = true
            CloudinaryUploader.subir(context, uri)
                .onSuccess {
                    store.agregar(uid, it); urls.add(0, it)
                    Toast.makeText(context, "Imagen subida", Toast.LENGTH_SHORT).show()
                }
                .onFailure { Toast.makeText(context, it.message ?: "Error al subir", Toast.LENGTH_LONG).show() }
            subiendo = false
        }
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary) }
            Text("Mis imágenes", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        }

        Button(
            onClick = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !subiendo,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Background),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) { Text(if (subiendo) "Subiendo…" else "Elegir de galería y subir", fontWeight = FontWeight.SemiBold) }

        if (!CloudinaryConfig.configurado) {
            Text(
                "Falta configurar CLOUD_NAME y UPLOAD_PRESET en CloudinaryConfig.",
                color = TextSecondary, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(20.dp)
            )
        }
        if (urls.isEmpty()) {
            Text("Aún no has subido imágenes.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(20.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(urls, key = { it }) { url ->
                AsyncImage(
                    model = CloudinaryUploader.optimizada(url),
                    contentDescription = "Imagen subida",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp)).background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(14.dp))
                )
            }
        }
    }
}
