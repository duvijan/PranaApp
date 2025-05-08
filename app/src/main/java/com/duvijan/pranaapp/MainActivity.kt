package com.duvijan.pranaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duvijan.pranaapp.ui.auth.LoginScreen
import com.duvijan.pranaapp.ui.auth.RegisterScreen
import com.duvijan.pranaapp.ui.breathing.BreathingScreen
import com.duvijan.pranaapp.ui.peace.PeaceMovementScreen
import com.duvijan.pranaapp.ui.theme.PranaAppTheme
import com.duvijan.pranaapp.util.AnalyticsManager
import com.duvijan.pranaapp.util.PeaceMeditationWorker
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Check if opened from peace meditation notification
        val fromPeaceMeditation = intent.getBooleanExtra(
            PeaceMeditationWorker.PEACE_MEDITATION_EXTRA, false
        )
        
        setContent {
            PranaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Set up navigation based on auth state and notification source
                    AppNavHost(
                        navController = navController,
                        startDestination = getStartDestination(fromPeaceMeditation),
                    )
                }
            }
        }
    }
    
    private fun getStartDestination(fromPeaceMeditation: Boolean): String {
        return when {
            // If opened from peace meditation notification, go to peace movement screen
            fromPeaceMeditation -> "peace_movement"
            // If user is logged in, go to breathing screen
            auth.currentUser != null -> "breathing"
            // Otherwise, go to login screen
            else -> "login"
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("breathing") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("breathing") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("breathing") {
            BreathingScreen(
                onPeaceMovementClick = {
                    navController.navigate("peace_movement")
                },
                onLogout = {
                    AnalyticsManager.logLogout()
                    navController.navigate("login") {
                        popUpTo("breathing") { inclusive = true }
                    }
                }
            )
        }
        
        composable("peace_movement") {
            PeaceMovementScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
