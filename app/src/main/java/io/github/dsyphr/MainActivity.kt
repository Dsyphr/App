package io.github.dsyphr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import io.github.dsyphr.dataClasses.User
import io.github.dsyphr.dataClasses.joe
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



        setContent {
            DsyphrTheme {
                Surface {
                    val navController = rememberNavController()

                    var start by remember {
                        mutableStateOf("")
                    }
                    if(Firebase.auth.currentUser != null){
                        start = "home"
                    }else{
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
                                onContactClick = { username -> navController.navigate("contact/$username") }, navController,
                                Firebase.auth.currentUser?.uid )

                        }
                        composable(
                            "contact/{username}", arguments = listOf(navArgument("username") { type = NavType.StringType }),
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
                            val username = it.arguments?.getString("username")
                            ChatScreen(secondUser = User(username.toString()), onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

