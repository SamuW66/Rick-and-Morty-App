package com.danidev.apprickmorty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.danidev.apprickmorty.data.model.RickCharacter
import com.danidev.apprickmorty.data.repository.CharacterRepository
import com.danidev.apprickmorty.ui.theme.Background
import com.danidev.apprickmorty.ui.theme.Primary
import com.danidev.apprickmorty.ui.theme.Secondary
import com.danidev.apprickmorty.ui.theme.SurfaceAlt
import com.danidev.apprickmorty.ui.theme.TextPrimary
import com.danidev.apprickmorty.ui.theme.TextSecondary

private data class Appearance(val title: String, val subtitle: String)

private val featuredAppearances = listOf(
    Appearance("La Poción de Rick", "Temporada 1 · Episodio 6"),
    Appearance("Encuentros Cercanos con Rick", "Temporada 1 · Episodio 10")
)

/**
 * Pantalla de detalle. Recibe el personaje ya cargado desde Home (evita
 * otra llamada a la red); si llega null (por ejemplo al entrar por deep
 * link) lo busca por id usando el repositorio.
 */
@Composable
fun CharacterDetailScreen(
    characterId: Int,
    initialCharacter: RickCharacter?,
    onBack: () -> Unit
) {
    var character by remember { mutableStateOf(initialCharacter) }
    var isFavorite by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(characterId) {
        if (character == null) {
            CharacterRepository().getCharacterById(characterId)
                .onSuccess { character = it }
                .onFailure { errorMessage = it.localizedMessage ?: "No se pudo cargar el personaje" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        DetailTopBar(
            isFavorite = isFavorite,
            onBack = onBack,
            onToggleFavorite = { isFavorite = !isFavorite }
        )

        val current = character
        when {
            current != null -> DetailContent(character = current)
            errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage.orEmpty(), color = TextSecondary)
            }
            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(icon = Icons.Filled.ArrowBack, contentDescription = "Volver", onClick = onBack)
        Text(
            text = "Detalle de Sujeto",
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge
        )
        RoundIconButton(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Favorito",
            tint = Primary,
            onClick = onToggleFavorite
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SurfaceAlt)
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun DetailContent(character: RickCharacter) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .border(width = 3.dp, color = Primary, shape = CircleShape)
                        .padding(6.dp)
                ) {
                    AsyncImage(
                        model = character.image,
                        contentDescription = character.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = character.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                val isAlive = character.status == "Alive"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary.copy(alpha = 0.15f))
                        .border(width = 1.dp, color = Primary, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ESTADO: ${if (isAlive) "VIVO" else character.status.uppercase()}",
                        color = Primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            InfoGrid(character = character)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Apariciones Destacadas",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(featuredAppearances) { appearance ->
            AppearanceRow(appearance = appearance)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { /* Navegación futura a dimensión de origen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = Background
                )
            ) {
                Text(
                    text = "VIAJAR A DIMENSIÓN DE ORIGEN",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun InfoGrid(character: RickCharacter) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Person,
                label = "Especie",
                value = character.species
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Public,
                label = "Origen",
                value = character.origin.name
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Science,
                label = "Tipo",
                value = character.type.ifBlank { "Sin clasificar" }
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LocationOn,
                label = "Ubicación",
                value = character.location.name
            )
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceAlt)
            .padding(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1
        )
    }
}

@Composable
private fun AppearanceRow(appearance: Appearance) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceAlt)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = appearance.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(text = appearance.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}
