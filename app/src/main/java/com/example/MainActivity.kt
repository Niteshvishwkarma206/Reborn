package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.RebornBottomNavigation
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.RebornViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val firebaseAuth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
        var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }

        DisposableEffect(firebaseAuth) {
          val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
          }
          firebaseAuth.addAuthStateListener(listener)
          onDispose {
            firebaseAuth.removeAuthStateListener(listener)
          }
        }

        if (currentUser == null) {
          AuthScreen(
            onAuthSuccess = {
              currentUser = firebaseAuth.currentUser
            }
          )
        } else {
          RebornApp()
        }
      }
    }
  }
}

@Composable
fun RebornApp(viewModel: RebornViewModel = viewModel()) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val missions by viewModel.missions.collectAsStateWithLifecycle()
  val exercises by viewModel.exercises.collectAsStateWithLifecycle()
  val selectedDay by viewModel.selectedChallengeDay.collectAsStateWithLifecycle()
  val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()
  val restSeconds by viewModel.restSecondsRemaining.collectAsStateWithLifecycle()
  val walkingActivities by viewModel.walkingActivities.collectAsStateWithLifecycle()
  val todayNutrition by viewModel.todayNutrition.collectAsStateWithLifecycle()
  val todayMindLog by viewModel.todayMindLog.collectAsStateWithLifecycle()
  val personalityTasks by viewModel.personalityTasks.collectAsStateWithLifecycle()
  val achievements by viewModel.achievements.collectAsStateWithLifecycle()
  val workoutHistory by viewModel.workoutHistory.collectAsStateWithLifecycle()
  val weeklySchedule = viewModel.weeklySchedule
  val xpToast by viewModel.xpToast.collectAsStateWithLifecycle()

  // Hardware/gesture BackHandler: Return to Home if on sub-screens
  BackHandler(enabled = currentScreen != AppScreen.HOME) {
    viewModel.navigateTo(AppScreen.HOME)
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = CleanBackground,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      RebornBottomNavigation(
        currentScreen = currentScreen,
        onScreenSelected = { screen ->
          viewModel.navigateTo(screen)
        }
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = paddingValues.calculateTopPadding())
    ) {
      // Screen Router
      when (currentScreen) {
        AppScreen.HOME -> DashboardScreen(
          viewModel = viewModel,
          user = userProfile,
          missions = missions,
          onNavigate = { screen -> viewModel.navigateTo(screen) }
        )
        AppScreen.PLAN, AppScreen.CHALLENGE, AppScreen.EXERCISES -> PlanScreen(
          viewModel = viewModel,
          selectedDay = selectedDay
        )
        AppScreen.WORKOUT -> if (activeWorkout != null) {
          WorkoutScreen(
            viewModel = viewModel,
            activeWorkout = activeWorkout,
            restSeconds = restSeconds,
            weeklySchedule = weeklySchedule,
            workoutHistory = workoutHistory
          )
        } else {
          PlanScreen(
            viewModel = viewModel,
            selectedDay = selectedDay
          )
        }
        AppScreen.ACTIVITY, AppScreen.WALKING -> ActivityScreen(
          viewModel = viewModel,
          user = userProfile,
          activities = walkingActivities
        )
        AppScreen.PROGRESS -> ProgressScreen(
          viewModel = viewModel,
          user = userProfile,
          missions = missions
        )
        AppScreen.PROFILE, AppScreen.SETTINGS -> SettingsScreen(
          viewModel = viewModel,
          user = userProfile,
          badges = achievements
        )
        AppScreen.NUTRITION -> NutritionScreen(
          viewModel = viewModel,
          user = userProfile,
          nutrition = todayNutrition
        )
        AppScreen.MIND -> MindScreen(
          viewModel = viewModel,
          mindLog = todayMindLog
        )
        AppScreen.PERSONALITY -> PersonalityScreen(
          viewModel = viewModel,
          tasks = personalityTasks
        )
      }

      // Floating XP Level Up / Mission Notification Banner
      AnimatedVisibility(
        visible = xpToast != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 12.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = CleanPrimary,
          border = androidx.compose.foundation.BorderStroke(1.dp, CleanPrimaryDark),
          shadowElevation = 4.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "✨", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = xpToast ?: "",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
