package io.github.dsyphr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.dsyphr.presentation.screen.contact.AddContactScreen
import io.github.dsyphr.presentation.screen.chat.ChatScreen
import io.github.dsyphr.presentation.screen.home.HomeScreen
import io.github.dsyphr.presentation.screen.login.LoginScreen
import io.github.dsyphr.presentation.screen.login.SignupScreen
import io.github.dsyphr.presentation.screen.settings.SettingsScreen
import io.github.dsyphr.presentation.viewmodel.HomeViewModel
import io.github.dsyphr.ui.theme.DsyphrTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        // Declare the launcher at the top of your Activity/Fragment:
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // TODO: Inform user that that your app will not show notifications.
            }
        }

        fun askNotificationPermission() {
            // This is only necessary for API level >= 33 (TIRAMISU)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // FCM SDK (and your app) can post notifications.
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // TODO: display an educational UI explaining to the user the features that will be enabled
                    //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                    //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                    //       If the user selects "No thanks," allow the user to continue without notifications.
                } else {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        askNotificationPermission()

        setContent {
            DsyphrTheme {
                Surface {
                    val navController = rememberNavController()
                    val homeViewModel: HomeViewModel = hiltViewModel()

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val startDestination = if (currentUser != null && currentUser.isEmailVerified) {
                        "home"
                    } else {
                        "login"
                    }

                    NavHost(navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                onNavigateToSignup = { navController.navigate("signup") },
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("signup") {
                            SignupScreen(
                                onSignupComplete = {
                                    navController.navigate("login") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val activeUser = FirebaseAuth.getInstance().currentUser

                            LaunchedEffect(activeUser?.uid) {
                                activeUser?.uid?.let { uid ->
                                    homeViewModel.setCurrentUserId(uid)
                                }
                            }

                            HomeScreen(
                                onNavigateToChat = { username, uid ->
                                    navController.navigate("chat/$uid/$username")
                                },
                                onNavigateToAddContact = { navController.navigate("addContact") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    homeViewModel.onLogout()
                                }
                            )
                        }

                        composable("addContact") {
                            AddContactScreen(
                                onBack = { navController.popBackStack() },
                                onContactAdded = {
                                    homeViewModel.loadContacts()
                                }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    homeViewModel.onLogout()
                                }
                            )
                        }

                        composable(
                            "chat/{uid}/{username}",
                            arguments = listOf(
                                navArgument("uid") { type = NavType.StringType },
                                navArgument("username") { type = NavType.StringType }
                            ),
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(durationMillis = 300)
                                )
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                        ) {
                            val uid = it.arguments?.getString("uid")
                            val username = it.arguments?.getString("username")

                            ChatScreen(
                                chatId = uid ?: "",
                                contactUsername = username ?: "Chat",
                                onBack = {
                                    navController.popBackStack()
                                    homeViewModel.clearNavigationTarget()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
