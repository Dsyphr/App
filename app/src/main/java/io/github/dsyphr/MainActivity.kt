package io.github.dsyphr

import android.Manifest
import android.app.Activity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import io.github.dsyphr.dataClasses.User
import io.github.dsyphr.screens.chat.ChatScreen
import io.github.dsyphr.screens.home.AddContact
import io.github.dsyphr.screens.home.HomeScreen
import io.github.dsyphr.screens.loginScreen.LoginScreen
import io.github.dsyphr.screens.loginScreen.SignupScreen
import io.github.dsyphr.ui.theme.DsyphrTheme


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

                    var start by remember {
                        mutableStateOf("")
                    }
                    if (Firebase.auth.currentUser != null && Firebase.auth.currentUser?.isEmailVerified == true) {
                        start = "home"
                    } else {
                        start = "login"
                    }


                    NavHost(navController, startDestination = start) {
                        composable(
                            "login",
                        ) {
                            LoginScreen(navController)
                        }
                        composable(
                            "addContact",
                        ) {
                            AddContact(navController, Firebase.auth.currentUser?.uid)
                        }
                        composable(
                            "signup",
                        ) {
                            SignupScreen(navController)
                        }
                        composable(
                            "home",
                        ) {
                            HomeScreen(
                                onContactClick = { username, uid -> navController.navigate("contact/$username/$uid") },
                                navController,
                                Firebase.auth.currentUser?.uid
                            )

                        }
                        composable(
                            "contact/{username}/{uid}",
                            arguments = listOf(
                                navArgument("username") {
                                    type = NavType.StringType
                                },
                                navArgument("uid"){
                                    type = NavType.StringType
                                }
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
                            },
                        ) {
                            val username = it.arguments?.getString("username")
                            val uid = it.arguments?.getString("uid")
                            ChatScreen(
                                secondUser = User(username.toString(), uid!!),
                                onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

