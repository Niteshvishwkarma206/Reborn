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
  HOME("Home", "home"),
  PLAN("Plan", "event_note"),
  ACTIVITY("Activity", "directions_walk"),
  PROGRESS("Progress", "insights"),
  PROFILE("Profile", "person"),

  // Backward-compatible screen entries
  CHALLENGE("Plan", "event_note"),
  EXERCISES("Exercises", "fitness_center"),
  WORKOUT("Workout", "timer"),
  WALKING("Activity", "directions_walk"),
  NUTRITION("Nutrition", "restaurant"),
  MIND("Mind", "psychology"),
  PERSONALITY("Habits", "check_circle"),
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
  val dailyWorkoutProgress = repository.dailyWorkoutProgress
  val stepTrackerState = repository.stepTrackerState
  val dailyHabits = repository.dailyHabits
  val dayWorkouts = repository.dayWorkouts
  val walkingActivities = repository.walkingActivities
  val todayNutrition = repository.todayNutrition
  val todayMindLog = repository.todayMindLog
  val personalityTasks = repository.personalityTasks
  val achievements = repository.achievements
  val workoutHistory = repository.workoutHistory
  val weeklySchedule = repository.weeklySchedule

  val healthConnectManager = repository.healthConnectManager

  private val _currentScreen = MutableStateFlow(AppScreen.HOME)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _selectedChallengeDay = MutableStateFlow(1)
  val selectedChallengeDay: StateFlow<Int> = _selectedChallengeDay.asStateFlow()

  // Active Workout & Rest Timer State
  private val _activeWorkout = MutableStateFlow<ActiveWorkoutSession?>(null)
  val activeWorkout: StateFlow<ActiveWorkoutSession?> = _activeWorkout.asStateFlow()

  private val _restSecondsRemaining = MutableStateFlow(0)
  val restSecondsRemaining: StateFlow<Int> = _restSecondsRemaining.asStateFlow()
  private var timerJob: Job? = null

  // Floating notification banner
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
    // Initial health connect availability check
    repository.refreshStepTracker()
  }

  fun signOut() {
    auth.signOut()
    repository.detachUserSync()
    showToast("Signed Out")
  }

  override fun onCleared() {
    super.onCleared()
    auth.removeAuthStateListener(authStateListener)
    repository.detachUserSync()
    timerJob?.cancel()
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun selectChallengeDay(day: Int) {
    _selectedChallengeDay.value = day
  }

  fun getWorkoutForDay(day: Int): DayWorkout = repository.getWorkoutForDay(day)

  fun getWorkoutProgressForDay(day: Int): DailyWorkoutProgress = repository.getWorkoutProgressForDay(day)

  fun toggleExerciseInWorkout(day: Int, exerciseId: String) {
    val result = repository.toggleExerciseInWorkout(day, exerciseId)
    if (result.isCompleted) {
      showToast("Day $day Workout Completed! (100%)")
    } else {
      showToast("Day $day Progress: ${result.percentage}%")
    }
  }

  fun toggleMission(mission: DailyMission) {
    repository.toggleMission(mission)
  }

  fun skipRestTimer() = stopRestTimer()
  fun finishWorkout() = finishActiveWorkout()
  fun cancelWorkout() = cancelActiveWorkout()

  // --- Step Tracker Methods ---

  fun refreshStepTracker() {
    repository.refreshStepTracker()
  }

  fun recordManualSteps(steps: Int, date: String = "Today") {
    repository.recordManualSteps(steps, date)
    showToast("Recorded $steps steps")
  }

  fun updateStepGoal(newGoal: Int) {
    repository.updateDailyStepGoal(newGoal)
    showToast("Step goal updated to $newGoal")
  }

  // --- Habits Methods ---

  fun toggleHabit(habitId: String) {
    repository.toggleHabit(habitId)
  }

  // --- Water & Nutrition ---

  fun addWater(ml: Int) {
    repository.addWater(ml)
    showToast(if (ml > 0) "+$ml ml Water logged" else "$ml ml Water updated")
  }

  fun logMeal(mealType: String, item: MealItem) {
    repository.logMeal(mealType, item)
    showToast("Logged ${item.name}")
  }

  fun removeMeal(mealType: String, index: Int) {
    repository.removeMeal(mealType, index)
  }

  fun recordWalking(activity: WalkingActivity) {
    repository.logWalkingActivity(activity)
    showToast("Session Recorded: ${activity.distanceKm} km")
  }

  fun saveMindLog(log: MindLog) {
    repository.saveMindLog(log)
    showToast("Mind Log Saved")
  }

  fun togglePersonalityTask(taskId: String) {
    repository.togglePersonalityTask(taskId)
  }

  fun updateTargets(waterTarget: Int, stepTarget: Int, kmTarget: Float) {
    repository.updateTargets(waterTarget, stepTarget, kmTarget)
    showToast("Goals Updated")
  }

  fun resetChallenge() {
    repository.resetChallenge()
    _selectedChallengeDay.value = 1
    showToast("Reset to Day 1")
  }

  // --- Workout Session & Rest Timer ---

  fun startWorkoutForDay(day: Int) {
    val workout = getWorkoutForDay(day)
    val sessionExercises = workout.exercises.map { ex ->
      ActiveWorkoutExercise(
        exercise = ex,
        sets = listOf(
          WorkoutSet(1, "12 reps"),
          WorkoutSet(2, "12 reps"),
          WorkoutSet(3, "10 reps")
        )
      )
    }
    _activeWorkout.value = ActiveWorkoutSession(
      workoutTitle = workout.title,
      exercises = sessionExercises
    )
    _selectedChallengeDay.value = day
    _currentScreen.value = AppScreen.WORKOUT
  }

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
    val targetSet = updatedSets[setIndex]

    updatedSets[setIndex] = targetSet.copy(
      completedReps = completedReps,
      weightKg = weightKg,
      isDone = true
    )
    updatedExList[exerciseIndex] = ex.copy(sets = updatedSets)

    val allDone = updatedExList.all { e -> e.sets.all { s -> s.isDone } }
    _activeWorkout.value = current.copy(
      exercises = updatedExList,
      isCompleted = allDone
    )

    if (!allDone) {
      startRestTimer(ex.exercise.restSeconds)
    }
  }

  fun startRestTimer(seconds: Int) {
    timerJob?.cancel()
    _restSecondsRemaining.value = seconds
    timerJob = viewModelScope.launch {
      while (_restSecondsRemaining.value > 0) {
        delay(1000)
        _restSecondsRemaining.value = _restSecondsRemaining.value - 1
      }
    }
  }

  fun stopRestTimer() {
    timerJob?.cancel()
    _restSecondsRemaining.value = 0
  }

  fun finishActiveWorkout() {
    val current = _activeWorkout.value ?: return
    repository.completeWorkout("Completed ${current.workoutTitle}")
    // Mark today's assigned workout completed as well
    val day = _selectedChallengeDay.value
    val workout = getWorkoutForDay(day)
    workout.exercises.forEach { ex ->
      val prog = getWorkoutProgressForDay(day)
      if (!prog.completedExerciseIds.contains(ex.id)) {
        repository.toggleExerciseInWorkout(day, ex.id)
      }
    }
    _activeWorkout.value = null
    showToast("Workout Completed!")
  }

  fun cancelActiveWorkout() {
    _activeWorkout.value = null
    stopRestTimer()
  }

  private fun showToast(msg: String) {
    _xpToast.value = msg
    viewModelScope.launch {
      delay(2600)
      if (_xpToast.value == msg) {
        _xpToast.value = null
      }
    }
  }
}
