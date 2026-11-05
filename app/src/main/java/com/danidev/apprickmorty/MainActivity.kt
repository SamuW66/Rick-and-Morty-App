package com.danidev.apprickmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danidev.apprickmorty.data.auth.AuthRepository
import com.danidev.apprickmorty.data.model.RickCharacter
import com.danidev.apprickmorty.ui.screens.CharacterDetailScreen
import com.danidev.apprickmorty.ui.screens.HardwareScreen
import com.danidev.apprickmorty.ui.screens.HomeScreen
import com.danidev.apprickmorty.ui.screens.ImagenesScreen
import com.danidev.apprickmorty.ui.screens.LoginScreen
import com.danidev.apprickmorty.ui.screens.SplashScreen
import com.danidev.apprickmorty.ui.theme.ApprickmortyTheme

private object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val IMAGES = "images"
    const val HARDWARE = "hardware"
    const val DETAIL = "detail/{characterId}"
    fun detail(id: Int) = "detail/$id"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApprickmortyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val auth = remember { AuthRepository() }
                    // Guardamos el último personaje tocado para no volver a
                    // pedirlo a la red al entrar al detalle.
                    var selectedCharacter by remember { mutableStateOf<RickCharacter?>(null) }

                    NavHost(
                        navController = navController,
                        startDestination = Routes.SPLASH,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.SPLASH) {
                            SplashScreen(
                                onStartAdventure = {
                                    // Con sesión guardada va directo al inicio; si no, al login.
                                    val destino = if (auth.estaLogueado) Routes.HOME else Routes.LOGIN
                                    navController.navigate(destino) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onAutenticado = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.HOME) {
                            HomeScreen(
                                userEmail = auth.email,
                                onHardwareClick = { navController.navigate(Routes.HARDWARE) },
                                onImagesClick = { navController.navigate(Routes.IMAGES) },
                                onLogout = {
                                    auth.cerrarSesion()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                },
                                onCharacterClick = { character ->
                                    selectedCharacter = character
                                    navController.navigate(Routes.detail(character.id))
                                }
                            )
                        }
                        composable(Routes.HARDWARE) {
                            HardwareScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Routes.IMAGES) {
                            ImagenesScreen(uid = auth.uid.orEmpty(), onBack = { navController.popBackStack() })
                        }
                        composable(
                            route = Routes.DETAIL,
                            arguments = listOf(navArgument("characterId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val characterId = backStackEntry.arguments?.getInt("characterId") ?: 0
                            val initial = selectedCharacter?.takeIf { it.id == characterId }
                            CharacterDetailScreen(
                                characterId = characterId,
                                initialCharacter = initial,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
