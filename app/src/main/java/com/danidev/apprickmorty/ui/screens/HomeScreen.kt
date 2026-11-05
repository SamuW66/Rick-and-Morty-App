package com.danidev.apprickmorty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.danidev.apprickmorty.data.model.RickCharacter
import com.danidev.apprickmorty.ui.theme.Background
import com.danidev.apprickmorty.ui.theme.Border
import com.danidev.apprickmorty.ui.theme.Primary
import com.danidev.apprickmorty.ui.theme.Secondary
import com.danidev.apprickmorty.ui.theme.Surface
import com.danidev.apprickmorty.ui.theme.SurfaceAlt
import com.danidev.apprickmorty.ui.theme.TextPrimary
import com.danidev.apprickmorty.ui.theme.TextSecondary
import com.danidev.apprickmorty.ui.viewmodel.CharacterUiState
import com.danidev.apprickmorty.ui.viewmodel.CharacterViewModel

private data class FeaturedEpisode(val title: String, val date: String, val tag: String)

private val featuredEpisodes = listOf(
    FeaturedEpisode("Piloto", "02 de Diciembre, 2013", "T1 · E01"),
    FeaturedEpisode("La Poción de Rick", "27 de Enero, 2014", "T1 · E06")
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel = viewModel(),
    onCharacterClick: (RickCharacter) -> Unit,
    userEmail: String = "",
    onHardwareClick: () -> Unit = {},
    onImagesClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            HomeTopBar(userEmail.substringBefore("@"))
            SearchBar()
            CategoryChips()

            when (val state = uiState) {
                is CharacterUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                is CharacterUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is CharacterUiState.Success -> {
                    FeaturedCharactersSection(
                        characters = state.characters.take(6),
                        onCharacterClick = onCharacterClick
                    )
                }
            }

            FeaturedEpisodesSection()
        }
        BottomNavBar(onHardwareClick, onImagesClick, onLogout)
    }
}

@Composable
private fun HomeTopBar(nombre: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Primary, Secondary))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = Background,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Dimensionante",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = nombre.ifBlank { "Rick Sanchez" },
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notificaciones",
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(width = 1.dp, color = Primary.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Buscar personajes, portales...",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun CategoryChips() {
    val categories = listOf("Personajes", "Planetas", "Episodios", "Dimensiones")
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { category ->
            val isActive = category == "Personajes"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) Primary else Surface)
                    .border(
                        width = 1.dp,
                        color = if (isActive) Primary else Border,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    color = if (isActive) Background else TextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun FeaturedCharactersSection(
    characters: List<RickCharacter>,
    onCharacterClick: (RickCharacter) -> Unit
) {
    SectionHeader(title = "Personajes Destacados")
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(characters, key = { it.id }) { character ->
            FeaturedCharacterCard(character = character, onClick = { onCharacterClick(character) })
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun FeaturedCharacterCard(character: RickCharacter, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceAlt)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = character.image,
                    contentDescription = character.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                val isAlive = character.status == "Alive"
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAlive) Primary else Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isAlive) "VIVO" else character.status.uppercase(),
                        color = Background,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = character.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = character.origin.name,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FeaturedEpisodesSection() {
    SectionHeader(title = "Episodios Destacados")
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        featuredEpisodes.forEach { episode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceAlt)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = episode.title,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = episode.date,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Secondary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = episode.tag,
                        color = Secondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Ver todo",
            color = Primary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun BottomNavBar(onHardwareClick: () -> Unit, onImagesClick: () -> Unit, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .border(width = 1.dp, color = Border)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomNavItem(icon = Icons.Filled.Home, label = "Inicio", isActive = true)
        BottomNavItem(icon = Icons.Outlined.Sensors, label = "Hardware", isActive = false, onClick = onHardwareClick)
        BottomNavItem(icon = Icons.Outlined.PhotoLibrary, label = "Imágenes", isActive = false, onClick = onImagesClick)
        BottomNavItem(icon = Icons.AutoMirrored.Outlined.Logout, label = "Salir", isActive = false, onClick = onLogout)
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) Primary else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) Primary else TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
