package com.danidev.apprickmorty.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danidev.apprickmorty.ui.theme.Background
import com.danidev.apprickmorty.ui.theme.Primary
import com.danidev.apprickmorty.ui.theme.Secondary
import com.danidev.apprickmorty.ui.theme.TextSecondary

/**
 * Pantalla de bienvenida: portal animado + CTA para entrar a Home.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onStartAdventure: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        PortalGraphic(modifier = Modifier.size(240.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PORTAL",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "MULTIVERSAL",
                color = Primary,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Wubba Lubba Dub Dub! Explora todas las dimensiones, personajes y locaciones de la serie.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartAdventure,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Background
                )
            ) {
                Text(
                    text = "INICIAR AVENTURA",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LICENCIA DE LA FEDERACIÓN GALÁCTICA",
                color = Secondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * Portal abstracto (anillos concéntricos con glow) usado en el splash.
 * Es una ilustración original hecha con Canvas, no un asset importado.
 */
@Composable
private fun PortalGraphic(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "portal")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portalRotation"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val radius = size.minDimension / 2f
            val glowBrush = Brush.radialGradient(
                colors = listOf(Primary.copy(alpha = 0.35f), Color.Transparent),
                radius = radius
            )
            drawCircle(brush = glowBrush, radius = radius)

            val ringColors = listOf(Primary, Secondary, Primary.copy(alpha = 0.6f))
            ringColors.forEachIndexed { index, color ->
                drawCircle(
                    color = color,
                    radius = radius - (index * 22).dp.toPx(),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Secondary, Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}
