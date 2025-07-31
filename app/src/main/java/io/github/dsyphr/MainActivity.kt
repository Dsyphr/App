package io.github.dsyphr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.dsyphr.dataClasses.joe
import io.github.dsyphr.screens.chat.ChatScreen
import io.github.dsyphr.screens.home.HomeScreen
import io.github.dsyphr.ui.theme.DsyphrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DsyphrTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "home") {
                        composable(
                            "home",
                        ) {
                            HomeScreen(onContactClick = { id -> navController.navigate("contact/$id") })
                        }
                        composable(
                            "contact/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType }),
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it }, animationSpec = tween(durationMillis = 300)
                                )
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it }, animationSpec = tween(durationMillis = 300)
                                )
                            },
                        ) {
                            ChatScreen(secondUser = joe, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

