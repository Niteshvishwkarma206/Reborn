package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.RebornRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen(val title: String, val iconName: String) {
  HOME("Command", "dashboard"),
  CHALLENGE("30-Day", "flag"),
  EXERCISES("Exercises", "fitness_center"),
  WORKOUT("Workout", "timer"),
  WALKING("Stride", "directions_walk"),
  NUTRITION("Fuel", "restaurant"),
  MIND("Mind", "psychology"),
  PERSONALITY("Persona", "military_tech"),
  PROGRESS("Analytics", "bar_chart"),
  SETTINGS("Profile", "person")
}

data class ActiveWorkoutSession(
  val workoutTitle: String,
  val currentExerciseIndex: Int = 0,
  val exercises: List<ActiveWorkoutExercise>,
  val isCompleted: Boolean = false
)

class RebornViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = RebornRepository(application.applicationContext)

  val userProfile = repository.userProfile
  val missions = repository.missions
  val exercises = repository.exercises
  val walkingActivities = repository.walkingActivities
  val todayNutrition = repository.todayNutrition
  val todayMindLog = repository.todayMindLog
  val personalityTasks = repository.personalityTasks
  val achievements = repository.achievements
  val workoutHistory = repository.workoutHistory
  val weeklySchedule = repository.weeklySchedule

  private val _currentScreen = MutableStateFlow(AppScreen.HOME)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _selectedChallengeDay = MutableStateFlow(3)
  val selectedChallengeDay: StateFlow<Int> = _selectedChallengeDay.asStateFlow()

  // Active Workout & Rest Timer State
  private val _activeWorkout = MutableStateFlow<ActiveWorkoutSession?>(null)
  val activeWorkout: StateFlow<ActiveWorkoutSession?> = _activeWorkout.asStateFlow()

  private val _restSecondsRemaining = MutableStateFlow(0)
  val restSecondsRemaining: StateFlow<Int> = _restSecondsRemaining.asStateFlow()
  private var timerJob: Job? = null

  // Floating notification / XP celebration banner
  private val _xpToast = MutableStateFlow<String?>(null)
  val xpToast: StateFlow<String?> = _xpToast.asStateFlow()

  private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

  private val authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
    val user = firebaseAuth.currentUser
    if (user != null) {
      repository.attachUserSync(user.uid)
    } else {
      repository.detachUserSync()
    }
  }

  init {
    auth.addAuthStateListener(authStateListener)
    val currentUser = auth.currentUser
    if (currentUser != null) {
      repository.attachUserSync(currentUser.uid)
    }
  }

  fun signOut() {
    auth.signOut()
    repository.detachUserSync()
    showXpToast("Signed Out of Reborn Network")
  }

  override fun onCleared() {
    super.onCleared()
    auth.removeAuthStateListener(authStateListener)
    repository.detachUserSync()
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun selectChallengeDay(day: Int) {
    _selectedChallengeDay.value = day
  }

  fun toggleMission(mission: DailyMission) {
    if (mission.isCompleted) {
      repository.uncompleteMission(mission.id)
    } else {
      val success = repository.completeMission(mission.id)
      if (success) {
        showXpToast("+${mission.xpReward} XP: ${mission.title}")
      }
    }
  }

  fun addWater(ml: Int) {
    repository.addWater(ml)
    showXpToast("+$ml ml Hydrated! (+10 XP)")
  }

  fun logMeal(mealType: String, item: MealItem) {
    repository.addMealItem(mealType, item)
    showXpToast("Logged ${item.name} (+15 XP)")
  }

  fun removeMeal(mealType: String, index: Int) {
    repository.removeMealItem(mealType, index)
  }

  fun recordWalking(activity: WalkingActivity) {
    repository.logWalkingActivity(activity)
    showXpToast("Stride Recorded: ${activity.distanceKm} km!")
  }

  fun saveMindLog(log: MindLog) {
    repository.saveMindLog(log)
    showXpToast("Mind Fortress Saved (+30 XP)")
  }

  fun togglePersonalityTask(taskId: String) {
    repository.togglePersonalityTask(taskId)
  }

  fun updateTargets(waterTarget: Int, stepTarget: Int, kmTarget: Float) {
    repository.updateTargets(waterTarget, stepTarget, kmTarget)
    showXpToast("Targets Updated!")
  }

  fun resetChallenge() {
    repository.resetChallenge()
    _selectedChallengeDay.value = 1
    showXpToast("System Reset to Day 1")
  }

  // Workout Session Execution
  fun startWorkout(daySchedule: DayWorkoutSchedule) {
    val allEx = exercises.value
    val sessionExercises = daySchedule.exerciseNames.map { name ->
      val matched = allEx.find { it.name.equals(name, ignoreCase = true) }
        ?: allEx.first()
      ActiveWorkoutExercise(
        exercise = matched,
        sets = listOf(
          WorkoutSet(1, "12 reps"),
          WorkoutSet(2, "12 reps"),
          WorkoutSet(3, "10 reps")
        )
      )
    }
    _activeWorkout.value = ActiveWorkoutSession(
      workoutTitle = "${daySchedule.dayName}: ${daySchedule.focusArea}",
      exercises = sessionExercises
    )
    _currentScreen.value = AppScreen.WORKOUT
  }

  fun completeSet(exerciseIndex: Int, setIndex: Int, completedReps: Int, weightKg: Float) {
    val current = _activeWorkout.value ?: return
    val updatedExList = current.exercises.toMutableList()
    val ex = updatedExList[exerciseIndex]
    val updatedSets = ex.sets.toMutableList()
    updatedSets[setIndex] = updatedSets[setIndex].copy(
      completedReps = completedReps,
      weightKg = weightKg,
      isDone = true
    )
    updatedExList[exerciseIndex] = ex.copy(sets = updatedSets)
    _activeWorkout.value = current.copy(exercises = updatedExList)

    startRestTimer(ex.exercise.restSeconds)
  }

  fun startRestTimer(seconds: Int) {
    timerJob?.cancel()
    _restSecondsRemaining.value = seconds
    timerJob = viewModelScope.launch {
      while (_restSecondsRemaining.value > 0) {
        delay(1000L)
        _restSecondsRemaining.value -= 1
      }
    }
  }

  fun skipRestTimer() {
    timerJob?.cancel()
    _restSecondsRemaining.value = 0
  }

  fun finishWorkout() {
    val current = _activeWorkout.value ?: return
    val summary = "Completed ${current.workoutTitle} • ${current.exercises.size} Exercises"
    repository.completeWorkout(summary)
    _activeWorkout.value = null
    timerJob?.cancel()
    _restSecondsRemaining.value = 0
    showXpToast("Workout Conquered! +100 XP ⚔️")
  }

  fun cancelWorkout() {
    _activeWorkout.value = null
    timerJob?.cancel()
    _restSecondsRemaining.value = 0
  }

  private fun showXpToast(msg: String) {
    _xpToast.value = msg
    viewModelScope.launch {
      delay(3000L)
      if (_xpToast.value == msg) {
        _xpToast.value = null
      }
    }
  }
}
