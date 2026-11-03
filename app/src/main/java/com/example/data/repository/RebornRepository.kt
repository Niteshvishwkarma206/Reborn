package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
import com.example.data.health.HealthConnectManager
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class RebornRepository(
  val db: FirebaseFirestore,
  private val context: Context
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.getString(R.string.firestore_database_id)
    ),
    context
  )

  init {
    try {
      db.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
        .build()
    } catch (_: Exception) {
      // Ignored if settings already locked or in test mode
    }
  }

  private val prefs: SharedPreferences =
    context.getSharedPreferences("reborn_user_prefs", Context.MODE_PRIVATE)

  val healthConnectManager = HealthConnectManager(context)
  private val scope = CoroutineScope(Dispatchers.IO)
  private var userDocListener: ListenerRegistration? = null
  private var missionsListener: ListenerRegistration? = null
  private var workoutsListener: ListenerRegistration? = null

  private val _userProfile = MutableStateFlow(loadUserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _missions = MutableStateFlow(loadMissions())
  val missions: StateFlow<List<DailyMission>> = _missions.asStateFlow()

  private val _exercises = MutableStateFlow(getInitialExercises())
  val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

  val dayWorkouts: List<DayWorkout> = generate30DayWorkouts(_exercises.value)

  private val _dailyWorkoutProgress = MutableStateFlow(loadDailyWorkoutProgress())
  val dailyWorkoutProgress: StateFlow<Map<Int, DailyWorkoutProgress>> = _dailyWorkoutProgress.asStateFlow()

  private val _stepTrackerState = MutableStateFlow(loadStepTrackerState())
  val stepTrackerState: StateFlow<StepTrackerState> = _stepTrackerState.asStateFlow()

  private val _dailyHabits = MutableStateFlow(loadDailyHabits())
  val dailyHabits: StateFlow<List<DailyHabit>> = _dailyHabits.asStateFlow()

  private val _walkingActivities = MutableStateFlow(loadWalkingActivities())
  val walkingActivities: StateFlow<List<WalkingActivity>> = _walkingActivities.asStateFlow()

  private val _todayNutrition = MutableStateFlow(loadTodayNutrition())
  val todayNutrition: StateFlow<NutritionEntry> = _todayNutrition.asStateFlow()

  private val _todayMindLog = MutableStateFlow(loadTodayMindLog())
  val todayMindLog: StateFlow<MindLog> = _todayMindLog.asStateFlow()

  private val _personalityTasks = MutableStateFlow(loadPersonalityTasks())
  val personalityTasks: StateFlow<List<PersonalityTask>> = _personalityTasks.asStateFlow()

  private val _achievements = MutableStateFlow(loadAchievements())
  val achievements: StateFlow<List<AchievementBadge>> = _achievements.asStateFlow()

  private val _workoutHistory = MutableStateFlow(loadWorkoutHistory())
  val workoutHistory: StateFlow<List<String>> = _workoutHistory.asStateFlow()

  val weeklySchedule: List<DayWorkoutSchedule> = listOf(
    DayWorkoutSchedule("Monday", "Full Body", listOf("Bodyweight Squats", "Incline Push-ups", "Forearm Plank", "Jumping Jacks"), 30),
    DayWorkoutSchedule("Tuesday", "Walking & Core", listOf("Brisk Walking 3km", "Forearm Plank", "Bird Dog", "Mountain Climbers"), 35),
    DayWorkoutSchedule("Wednesday", "Chest & Arms", listOf("Wall Push-ups", "Incline Push-ups", "Arm Circles", "Doorway Rows"), 25),
    DayWorkoutSchedule("Thursday", "Walking & Mobility", listOf("Active Walk 4km", "Hamstring Stretch", "Cobra Stretch", "Cat-Cow Stretch"), 40),
    DayWorkoutSchedule("Friday", "Legs & Core", listOf("Bodyweight Squats", "Walking Lunges", "Glute Bridges", "Calf Raises"), 30),
    DayWorkoutSchedule("Saturday", "Full Body Tone", listOf("Bird Dog", "Glute Bridges", "Wall Push-ups", "Torso Twists"), 25),
    DayWorkoutSchedule("Sunday", "Active Recovery", listOf("Deep Breathing Meditation", "Full Body Stretching", "Hydration Protocol"), 20)
  )

  // Start syncing user data when authenticated
  fun attachUserSync(userId: String) {
    detachUserSync()
    val userRef = db.collection("users").document(userId)

    userDocListener = userRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w("RebornRepo", "User profile sync error: ${error.message}")
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        val level = snapshot.getLong("level")?.toInt() ?: _userProfile.value.level
        val currentXp = snapshot.getLong("currentXp")?.toInt() ?: _userProfile.value.currentXp
        val xpNeeded = snapshot.getLong("xpToNextLevel")?.toInt() ?: _userProfile.value.xpToNextLevel
        val day = snapshot.getLong("currentChallengeDay")?.toInt() ?: _userProfile.value.currentChallengeDay
        val streak = snapshot.getLong("streakDays")?.toInt() ?: _userProfile.value.streakDays
        val name = snapshot.getString("name") ?: _userProfile.value.name
        val title = snapshot.getString("title") ?: _userProfile.value.title
        val rank = snapshot.getString("rankTier") ?: _userProfile.value.rankTier
        val stepTarget = snapshot.getLong("dailyStepTarget")?.toInt() ?: _userProfile.value.dailyStepTarget
        val waterTarget = snapshot.getLong("dailyWaterTargetMl")?.toInt() ?: _userProfile.value.dailyWaterTargetMl

        val updated = _userProfile.value.copy(
          name = name,
          title = title,
          rankTier = rank,
          level = level,
          currentXp = currentXp,
          xpToNextLevel = xpNeeded,
          currentChallengeDay = day,
          streakDays = streak,
          dailyStepTarget = stepTarget,
          dailyWaterTargetMl = waterTarget
        )
        _userProfile.value = updated
        saveUserProfile(updated)

        // Update step tracker daily goal
        _stepTrackerState.value = _stepTrackerState.value.copy(dailyGoal = stepTarget)
      } else {
        syncUserProfileToCloud(_userProfile.value, userId)
      }
    }

    // Sync workout progress collection
    workoutsListener = userRef.collection("workouts").addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.w("RebornRepo", "Workouts sync error: ${error.message}")
        return@addSnapshotListener
      }
      if (snapshots != null && !snapshots.isEmpty) {
        val currentProgress = _dailyWorkoutProgress.value.toMutableMap()
        for (doc in snapshots.documents) {
          val day = doc.getLong("day")?.toInt() ?: continue
          val total = doc.getLong("totalExercises")?.toInt() ?: 0
          val completed = doc.getLong("completedExercises")?.toInt() ?: 0
          val percentage = doc.getLong("percentage")?.toInt() ?: 0
          val isRest = doc.getBoolean("isRestDay") ?: false
          val isDone = doc.getBoolean("isCompleted") ?: false
          val completedAt = doc.getLong("completedAt")
          val rawIds = doc.get("completedExerciseIds") as? List<*>
          val completedIds = rawIds?.filterIsInstance<String>() ?: emptyList()
          val title = doc.getString("summary") ?: "Day $day Workout"

          currentProgress[day] = DailyWorkoutProgress(
            day = day,
            workoutTitle = title,
            isRestDay = isRest,
            totalExercises = total,
            completedExercises = completed,
            completedExerciseIds = completedIds,
            percentage = percentage,
            isCompleted = isDone,
            completedAt = completedAt
          )
        }
        _dailyWorkoutProgress.value = currentProgress
        saveDailyWorkoutProgress(currentProgress)
      }
    }

    // Observe user missions
    missionsListener = userRef.collection("missions").addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.w("RebornRepo", "Missions sync error: ${error.message}")
        return@addSnapshotListener
      }
      if (snapshots != null && !snapshots.isEmpty) {
        val currentList = _missions.value.toMutableList()
        for (doc in snapshots.documents) {
          val id = doc.getString("id") ?: continue
          val isDone = doc.getBoolean("isCompleted") ?: false
          val completedAt = doc.getLong("completedAt")
          val idx = currentList.indexOfFirst { it.id == id }
          if (idx != -1) {
            currentList[idx] = currentList[idx].copy(isCompleted = isDone, completedAt = completedAt)
          }
        }
        _missions.value = currentList
        saveMissions(currentList)
      }
    }
  }

  fun detachUserSync() {
    userDocListener?.remove()
    userDocListener = null
    workoutsListener?.remove()
    workoutsListener = null
    missionsListener?.remove()
    missionsListener = null
  }

  // --- Daily Workout Progress Logic ---

  fun getWorkoutForDay(day: Int): DayWorkout {
    return dayWorkouts.getOrNull(day - 1) ?: DayWorkout(day, "Day $day Workout", "General Fitness")
  }

  fun getWorkoutProgressForDay(day: Int): DailyWorkoutProgress {
    return _dailyWorkoutProgress.value[day] ?: run {
      val workout = getWorkoutForDay(day)
      DailyWorkoutProgress(
        day = day,
        workoutTitle = workout.title,
        isRestDay = workout.isRestDay,
        totalExercises = workout.exercises.size,
        completedExercises = 0,
        completedExerciseIds = emptyList(),
        percentage = if (workout.isRestDay) 100 else 0,
        isCompleted = workout.isRestDay
      )
    }
  }

  fun toggleMission(mission: DailyMission) {
    val current = _missions.value.toMutableList()
    val index = current.indexOfFirst { it.id == mission.id }
    if (index >= 0) {
      val isNowCompleted = !current[index].isCompleted
      current[index] = current[index].copy(
        isCompleted = isNowCompleted,
        completedAt = if (isNowCompleted) System.currentTimeMillis() else null
      )
      _missions.value = current
      saveMissions(current)
    }
  }

  fun toggleExerciseInWorkout(day: Int, exerciseId: String): DailyWorkoutProgress {
    val workout = getWorkoutForDay(day)
    val existing = getWorkoutProgressForDay(day)

    val currentCompletedIds = existing.completedExerciseIds.toMutableList()
    if (currentCompletedIds.contains(exerciseId)) {
      currentCompletedIds.remove(exerciseId)
    } else {
      currentCompletedIds.add(exerciseId)
    }

    val total = workout.exercises.size
    val completedCount = currentCompletedIds.size
    val percentage = if (total > 0) ((completedCount.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100) else 0
    val isDone = (completedCount == total && total > 0) || workout.isRestDay
    val completedAt = if (isDone) System.currentTimeMillis() else null

    val updatedProgress = existing.copy(
      day = day,
      workoutTitle = workout.title,
      isRestDay = workout.isRestDay,
      totalExercises = total,
      completedExercises = completedCount,
      completedExerciseIds = currentCompletedIds,
      percentage = percentage,
      isCompleted = isDone,
      completedAt = completedAt
    )

    val currentMap = _dailyWorkoutProgress.value.toMutableMap()
    currentMap[day] = updatedProgress
    _dailyWorkoutProgress.value = currentMap
    saveDailyWorkoutProgress(currentMap)

    // Sync to Firestore under signed-in user UID
    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      scope.launch {
        try {
          db.collection("users").document(uid).collection("workouts").document("day_$day").set(
            mapOf(
              "id" to "day_$day",
              "day" to day,
              "summary" to workout.title,
              "totalExercises" to total,
              "completedExercises" to completedCount,
              "percentage" to percentage,
              "isRestDay" to workout.isRestDay,
              "isCompleted" to isDone,
              "completedExerciseIds" to currentCompletedIds,
              "completedAt" to (completedAt ?: 0L),
              "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
          )
        } catch (e: Exception) {
          Log.e("RebornRepo", "Failed to sync workout progress to Firestore: ${e.message}")
        }
      }
    }

    return updatedProgress
  }

  // --- Step Tracker Logic ---

  fun refreshStepTracker() {
    val isAvailable = healthConnectManager.isAvailable()
    _stepTrackerState.value = _stepTrackerState.value.copy(
      isHealthConnectAvailable = isAvailable,
      dailyGoal = _userProfile.value.dailyStepTarget
    )

    if (isAvailable) {
      scope.launch {
        val hasPerms = healthConnectManager.hasPermissions()
        if (hasPerms) {
          val todaySteps = healthConnectManager.readTodaySteps().toInt()
          val weekly = healthConnectManager.readWeeklySteps(_userProfile.value.dailyStepTarget)
          val distance = todaySteps * 0.00075f
          _stepTrackerState.value = _stepTrackerState.value.copy(
            todaySteps = todaySteps,
            distanceKm = distance,
            source = StepSource.HEALTH_CONNECT,
            isPermissionGranted = true,
            weeklyHistory = weekly,
            isLoading = false
          )
          saveStepTrackerState(_stepTrackerState.value)
        } else {
          _stepTrackerState.value = _stepTrackerState.value.copy(
            isPermissionGranted = false,
            isLoading = false
          )
        }
      }
    }
  }

  fun recordManualSteps(steps: Int, date: String = "Today") {
    val cleanSteps = steps.coerceAtLeast(0)
    val distance = cleanSteps * 0.00075f
    val currentWeekly = _stepTrackerState.value.weeklyHistory.toMutableList()
    val todayName = LocalDate.now().dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

    val idx = currentWeekly.indexOfFirst { it.dayOfWeek.equals(todayName, ignoreCase = true) }
    val entry = DailyStepCount(
      dayOfWeek = todayName,
      date = LocalDate.now().toString(),
      steps = cleanSteps,
      goal = _userProfile.value.dailyStepTarget,
      source = StepSource.MANUAL
    )
    if (idx != -1) {
      currentWeekly[idx] = entry
    } else {
      currentWeekly.add(entry)
    }

    val updatedState = _stepTrackerState.value.copy(
      todaySteps = cleanSteps,
      distanceKm = distance,
      source = StepSource.MANUAL,
      weeklyHistory = currentWeekly
    )
    _stepTrackerState.value = updatedState
    saveStepTrackerState(updatedState)

    // Save as WalkingActivity record
    logWalkingActivity(
      WalkingActivity(
        id = "steps_${System.currentTimeMillis()}",
        date = date,
        activityType = "Walking",
        distanceKm = distance,
        durationMinutes = (cleanSteps / 100).coerceAtLeast(10),
        steps = cleanSteps,
        notes = "Manual step entry"
      )
    )
  }

  fun updateDailyStepGoal(newGoal: Int) {
    val goal = newGoal.coerceAtLeast(1000)
    val updatedUser = _userProfile.value.copy(dailyStepTarget = goal)
    _userProfile.value = updatedUser
    saveUserProfile(updatedUser)

    _stepTrackerState.value = _stepTrackerState.value.copy(dailyGoal = goal)
    saveStepTrackerState(_stepTrackerState.value)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      syncUserProfileToCloud(updatedUser, uid)
    }
  }

  // --- Habits Logic ---

  fun toggleHabit(habitId: String) {
    val current = _dailyHabits.value.toMutableList()
    val idx = current.indexOfFirst { it.id == habitId }
    if (idx != -1) {
      val habit = current[idx]
      current[idx] = habit.copy(isCompleted = !habit.isCompleted)
      _dailyHabits.value = current
      saveDailyHabits(current)

      val uid = Firebase.auth.currentUser?.uid
      if (uid != null) {
        scope.launch {
          try {
            db.collection("users").document(uid).collection("habits").document("today").set(
              mapOf(
                "date" to LocalDate.now().toString(),
                "completedCount" to current.count { it.isCompleted },
                "habits" to current.map { mapOf("id" to it.id, "isCompleted" to it.isCompleted) },
                "updatedAt" to FieldValue.serverTimestamp()
              ),
              SetOptions.merge()
            )
          } catch (e: Exception) {
            Log.e("RebornRepo", "Failed to sync habits to Firestore: ${e.message}")
          }
        }
      }
    }
  }

  // --- Mission & Milestone Logic ---

  fun completeMission(missionId: String): Boolean {
    val currentMissions = _missions.value.toMutableList()
    val index = currentMissions.indexOfFirst { it.id == missionId }
    if (index != -1 && !currentMissions[index].isCompleted) {
      val mission = currentMissions[index]
      val now = System.currentTimeMillis()
      val updatedMission = mission.copy(isCompleted = true, completedAt = now)
      currentMissions[index] = updatedMission
      _missions.value = currentMissions
      saveMissions(currentMissions)

      awardXp(mission.xpReward)
      checkAchievements()

      val uid = Firebase.auth.currentUser?.uid
      if (uid != null) {
        scope.launch {
          try {
            db.collection("users").document(uid).collection("missions").document(missionId)
              .set(
                mapOf(
                  "id" to updatedMission.id,
                  "day" to updatedMission.day,
                  "title" to updatedMission.title,
                  "category" to updatedMission.category.name,
                  "xpReward" to updatedMission.xpReward,
                  "isCompleted" to true,
                  "completedAt" to now,
                  "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
              )
          } catch (e: Exception) {
            Log.e("RebornRepo", "Failed to sync mission completion to Firestore: ${e.message}")
          }
        }
      }
      return true
    }
    return false
  }

  fun awardXp(amount: Int) {
    val user = _userProfile.value
    var newXp = user.currentXp + amount
    var newLevel = user.level
    var xpNeeded = user.xpToNextLevel

    while (newXp >= xpNeeded) {
      newXp -= xpNeeded
      newLevel++
      xpNeeded = (xpNeeded * 1.25f).toInt()
    }

    val updatedUser = user.copy(
      level = newLevel,
      currentXp = newXp,
      xpToNextLevel = xpNeeded
    )
    _userProfile.value = updatedUser
    saveUserProfile(updatedUser)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      syncUserProfileToCloud(updatedUser, uid)
    }
  }

  fun syncUserProfileToCloud(user: UserProfile, userId: String) {
    scope.launch {
      try {
        db.collection("users").document(userId).set(
          mapOf(
            "userId" to userId,
            "name" to user.name,
            "email" to user.email,
            "title" to user.title,
            "rankTier" to user.rankTier,
            "level" to user.level,
            "currentXp" to user.currentXp,
            "xpToNextLevel" to user.xpToNextLevel,
            "currentChallengeDay" to user.currentChallengeDay,
            "streakDays" to user.streakDays,
            "dailyWaterTargetMl" to user.dailyWaterTargetMl,
            "dailyStepTarget" to user.dailyStepTarget,
            "dailyKmTarget" to user.dailyKmTarget,
            "startDate" to user.startDate,
            "updatedAt" to FieldValue.serverTimestamp()
          ),
          SetOptions.merge()
        )
      } catch (e: Exception) {
        Log.e("RebornRepo", "Cloud user profile sync failed: ${e.message}")
      }
    }
  }

  fun addWater(ml: Int) {
    val current = _todayNutrition.value
    val newTotal = (current.waterMl + ml).coerceAtLeast(0)
    val updated = current.copy(waterMl = newTotal)
    _todayNutrition.value = updated
    saveNutrition(updated)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      scope.launch {
        try {
          db.collection("users").document(uid).collection("nutrition").document("today").set(
            mapOf(
              "date" to updated.date,
              "waterMl" to updated.waterMl,
              "notes" to updated.notes,
              "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
          )
        } catch (e: Exception) {
          Log.e("RebornRepo", "Failed to sync water to Firestore: ${e.message}")
        }
      }
    }
  }

  fun logMeal(mealType: String, item: MealItem) {
    val current = _todayNutrition.value
    val updated = when (mealType.lowercase()) {
      "breakfast" -> current.copy(breakfast = current.breakfast + item)
      "lunch" -> current.copy(lunch = current.lunch + item)
      "evening snack", "snack" -> current.copy(eveningSnack = current.eveningSnack + item)
      "dinner" -> current.copy(dinner = current.dinner + item)
      else -> current.copy(lunch = current.lunch + item)
    }
    _todayNutrition.value = updated
    saveNutrition(updated)
  }

  fun removeMeal(mealType: String, index: Int) {
    val current = _todayNutrition.value
    val updated = when (mealType.lowercase()) {
      "breakfast" -> current.copy(breakfast = current.breakfast.filterIndexed { i, _ -> i != index })
      "lunch" -> current.copy(lunch = current.lunch.filterIndexed { i, _ -> i != index })
      "evening snack", "snack" -> current.copy(eveningSnack = current.eveningSnack.filterIndexed { i, _ -> i != index })
      "dinner" -> current.copy(dinner = current.dinner.filterIndexed { i, _ -> i != index })
      else -> current
    }
    _todayNutrition.value = updated
    saveNutrition(updated)
  }

  fun logWalkingActivity(activity: WalkingActivity) {
    val updated = listOf(activity) + _walkingActivities.value
    _walkingActivities.value = updated
    saveWalkingActivities(updated)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      scope.launch {
        try {
          db.collection("users").document(uid).collection("strides").document(activity.id).set(
            mapOf(
              "id" to activity.id,
              "activityType" to activity.activityType,
              "distanceKm" to activity.distanceKm,
              "durationMinutes" to activity.durationMinutes,
              "steps" to activity.steps,
              "date" to activity.date,
              "notes" to activity.notes,
              "createdAt" to FieldValue.serverTimestamp()
            )
          )
        } catch (e: Exception) {
          Log.e("RebornRepo", "Failed to sync stride to Firestore: ${e.message}")
        }
      }
    }
  }

  fun saveMindLog(log: MindLog) {
    _todayMindLog.value = log
    saveMindLogToPrefs(log)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      scope.launch {
        try {
          db.collection("users").document(uid).collection("mind").document("today").set(
            mapOf(
              "date" to log.date,
              "mood" to log.mood,
              "focusScore" to log.focusScore,
              "confidenceScore" to log.confidenceScore,
              "meditationMinutes" to log.meditationMinutes,
              "readingPages" to log.readingPages,
              "journalText" to log.journalText,
              "reflectionText" to log.reflectionText,
              "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
          )
        } catch (e: Exception) {
          Log.e("RebornRepo", "Failed to sync mind log to Firestore: ${e.message}")
        }
      }
    }
  }

  fun togglePersonalityTask(taskId: String) {
    val current = _personalityTasks.value.toMutableList()
    val index = current.indexOfFirst { it.id == taskId }
    if (index != -1) {
      val task = current[index]
      val newState = !task.isCompleted
      current[index] = task.copy(isCompleted = newState)
      _personalityTasks.value = current
      savePersonalityTasks(current)
    }
  }

  fun completeWorkout(workoutSummary: String) {
    val current = listOf(workoutSummary) + _workoutHistory.value
    _workoutHistory.value = current
    saveWorkoutHistory(current)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      scope.launch {
        try {
          val wid = "workout_${System.currentTimeMillis()}"
          db.collection("users").document(uid).collection("workouts").document(wid).set(
            mapOf(
              "id" to wid,
              "summary" to workoutSummary,
              "completedAt" to FieldValue.serverTimestamp()
            )
          )
        } catch (e: Exception) {
          Log.e("RebornRepo", "Failed to sync workout to Firestore: ${e.message}")
        }
      }
    }
  }

  fun updateTargets(waterMl: Int, steps: Int, km: Float) {
    val user = _userProfile.value
    val updated = user.copy(
      dailyWaterTargetMl = waterMl,
      dailyStepTarget = steps,
      dailyKmTarget = km
    )
    _userProfile.value = updated
    saveUserProfile(updated)

    _stepTrackerState.value = _stepTrackerState.value.copy(dailyGoal = steps)
    saveStepTrackerState(_stepTrackerState.value)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      syncUserProfileToCloud(updated, uid)
    }
  }

  fun resetChallenge() {
    val resetProgress = mutableMapOf<Int, DailyWorkoutProgress>()
    _dailyWorkoutProgress.value = resetProgress
    saveDailyWorkoutProgress(resetProgress)

    val initialMissions = getInitialMissions()
    _missions.value = initialMissions
    saveMissions(initialMissions)

    val resetUser = UserProfile()
    _userProfile.value = resetUser
    saveUserProfile(resetUser)

    _walkingActivities.value = emptyList()
    saveWalkingActivities(emptyList())

    _workoutHistory.value = emptyList()
    saveWorkoutHistory(emptyList())

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      syncUserProfileToCloud(resetUser, uid)
    }
  }

  private fun checkAchievements() {
    val badges = _achievements.value.toMutableList()
    val workoutCount = _workoutHistory.value.size
    val totalKm = _walkingActivities.value.sumOf { it.distanceKm.toDouble() }.toFloat()

    fun unlock(badgeId: String) {
      val idx = badges.indexOfFirst { it.id == badgeId }
      if (idx != -1 && !badges[idx].isUnlocked) {
        badges[idx] = badges[idx].copy(isUnlocked = true, unlockedDate = "Unlocked Today")
      }
    }

    if (workoutCount >= 1) unlock("first_workout")
    if (workoutCount >= 5) unlock("workout_5")
    if (totalKm >= 10.0f) unlock("distance_10")
    if (totalKm >= 50.0f) unlock("distance_50")

    _achievements.value = badges
    saveAchievements(badges)
  }

  // --- Persistence Helpers ---

  private fun loadDailyWorkoutProgress(): Map<Int, DailyWorkoutProgress> {
    val json = prefs.getString("cached_workout_progress", null) ?: return emptyMap()
    return try {
      val obj = JSONObject(json)
      val map = mutableMapOf<Int, DailyWorkoutProgress>()
      val keys = obj.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        val day = key.toIntOrNull() ?: continue
        val itemObj = obj.getJSONObject(key)
        val rawIds = itemObj.optJSONArray("completedExerciseIds")
        val ids = mutableListOf<String>()
        if (rawIds != null) {
          for (i in 0 until rawIds.length()) ids.add(rawIds.getString(i))
        }
        map[day] = DailyWorkoutProgress(
          day = day,
          workoutTitle = itemObj.optString("workoutTitle", "Day $day Workout"),
          isRestDay = itemObj.optBoolean("isRestDay", false),
          totalExercises = itemObj.optInt("totalExercises", 0),
          completedExercises = itemObj.optInt("completedExercises", 0),
          completedExerciseIds = ids,
          percentage = itemObj.optInt("percentage", 0),
          isCompleted = itemObj.optBoolean("isCompleted", false),
          completedAt = if (itemObj.has("completedAt")) itemObj.getLong("completedAt") else null
        )
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  private fun saveDailyWorkoutProgress(map: Map<Int, DailyWorkoutProgress>) {
    try {
      val obj = JSONObject()
      for ((day, prog) in map) {
        val itemObj = JSONObject().apply {
          put("day", prog.day)
          put("workoutTitle", prog.workoutTitle)
          put("isRestDay", prog.isRestDay)
          put("totalExercises", prog.totalExercises)
          put("completedExercises", prog.completedExercises)
          put("percentage", prog.percentage)
          put("isCompleted", prog.isCompleted)
          if (prog.completedAt != null) put("completedAt", prog.completedAt)
          val arr = JSONArray()
          prog.completedExerciseIds.forEach { arr.put(it) }
          put("completedExerciseIds", arr)
        }
        obj.put(day.toString(), itemObj)
      }
      prefs.edit().putString("cached_workout_progress", obj.toString()).apply()
    } catch (e: Exception) {
      Log.e("RebornRepo", "Error saving workout progress: ${e.message}")
    }
  }

  private fun loadStepTrackerState(): StepTrackerState {
    val steps = prefs.getInt("cached_today_steps", 0)
    val goal = prefs.getInt("user_step_target", 8000)
    val srcStr = prefs.getString("cached_step_source", StepSource.NONE.name) ?: StepSource.NONE.name
    val source = try { StepSource.valueOf(srcStr) } catch (_: Exception) { StepSource.NONE }
    return StepTrackerState(
      todaySteps = steps,
      dailyGoal = goal,
      distanceKm = steps * 0.00075f,
      source = source
    )
  }

  private fun saveStepTrackerState(state: StepTrackerState) {
    prefs.edit()
      .putInt("cached_today_steps", state.todaySteps)
      .putString("cached_step_source", state.source.name)
      .apply()
  }

  private fun loadDailyHabits(): List<DailyHabit> {
    return listOf(
      DailyHabit("h_workout", "Complete Today's Assigned Workout", "Fitness", false),
      DailyHabit("h_steps", "Hit 8,000 Daily Steps Goal", "Activity", false),
      DailyHabit("h_water", "Drink 3 Liters Clean Water", "Hydration", false),
      DailyHabit("h_stretch", "10-Min Mobility / Posture Stretch", "Recovery", false),
      DailyHabit("h_meal", "Nutritious High-Protein Fuel", "Nutrition", false)
    )
  }

  private fun saveDailyHabits(habits: List<DailyHabit>) {
    // cached in prefs
  }

  private fun loadUserProfile(): UserProfile {
    return UserProfile(
      name = prefs.getString("user_name", "Reborn Member") ?: "Reborn Member",
      email = prefs.getString("user_email", "member@reborn.app") ?: "member@reborn.app",
      title = prefs.getString("user_title", "Transformation Plan") ?: "Transformation Plan",
      rankTier = prefs.getString("user_rank", "Active") ?: "Active",
      level = prefs.getInt("user_level", 1),
      currentXp = prefs.getInt("user_current_xp", 100),
      xpToNextLevel = prefs.getInt("user_xp_needed", 500),
      currentChallengeDay = prefs.getInt("user_challenge_day", 1),
      streakDays = prefs.getInt("user_streak", 1),
      dailyWaterTargetMl = prefs.getInt("user_water_target", 3000),
      dailyStepTarget = prefs.getInt("user_step_target", 8000),
      dailyKmTarget = prefs.getFloat("user_km_target", 5.0f)
    )
  }

  private fun saveUserProfile(user: UserProfile) {
    prefs.edit()
      .putString("user_name", user.name)
      .putString("user_email", user.email)
      .putString("user_title", user.title)
      .putString("user_rank", user.rankTier)
      .putInt("user_level", user.level)
      .putInt("user_current_xp", user.currentXp)
      .putInt("user_xp_needed", user.xpToNextLevel)
      .putInt("user_challenge_day", user.currentChallengeDay)
      .putInt("user_streak", user.streakDays)
      .putInt("user_water_target", user.dailyWaterTargetMl)
      .putInt("user_step_target", user.dailyStepTarget)
      .putFloat("user_km_target", user.dailyKmTarget)
      .apply()
  }

  private fun loadMissions(): List<DailyMission> {
    val json = prefs.getString("cached_missions", null)
    if (json.isNullOrEmpty()) {
      return getInitialMissions()
    }
    return try {
      val array = JSONArray(json)
      val list = mutableListOf<DailyMission>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          DailyMission(
            id = obj.getString("id"),
            day = obj.getInt("day"),
            title = obj.getString("title"),
            description = obj.getString("description"),
            category = MissionCategory.valueOf(obj.getString("category")),
            xpReward = obj.getInt("xpReward"),
            isCompleted = obj.getBoolean("isCompleted"),
            completedAt = if (obj.has("completedAt")) obj.getLong("completedAt") else null
          )
        )
      }
      list
    } catch (_: Exception) {
      getInitialMissions()
    }
  }

  private fun saveMissions(list: List<DailyMission>) {
    val array = JSONArray()
    for (m in list) {
      val obj = JSONObject().apply {
        put("id", m.id)
        put("day", m.day)
        put("title", m.title)
        put("description", m.description)
        put("category", m.category.name)
        put("xpReward", m.xpReward)
        put("isCompleted", m.isCompleted)
        if (m.completedAt != null) put("completedAt", m.completedAt)
      }
      array.put(obj)
    }
    prefs.edit().putString("cached_missions", array.toString()).apply()
  }

  private fun loadWalkingActivities(): List<WalkingActivity> {
    return emptyList()
  }

  private fun saveWalkingActivities(list: List<WalkingActivity>) {
    // save
  }

  private fun loadTodayNutrition(): NutritionEntry {
    return NutritionEntry(
      date = "Today",
      waterMl = prefs.getInt("cached_water_ml", 0)
    )
  }

  private fun saveNutrition(entry: NutritionEntry) {
    prefs.edit().putInt("cached_water_ml", entry.waterMl).apply()
  }

  private fun loadTodayMindLog(): MindLog = MindLog(date = "Today")
  private fun saveMindLogToPrefs(log: MindLog) {}
  private fun loadPersonalityTasks(): List<PersonalityTask> = emptyList()
  private fun savePersonalityTasks(list: List<PersonalityTask>) {}
  private fun loadAchievements(): List<AchievementBadge> = emptyList()
  private fun saveAchievements(list: List<AchievementBadge>) {}
  private fun loadWorkoutHistory(): List<String> = emptyList()
  private fun saveWorkoutHistory(list: List<String>) {}

  // --- Pre-seeded Exercises (All 7 Categories with Verified Demo Video Links) ---

  private fun getInitialExercises(): List<Exercise> {
    return listOf(
      // Full Body
      Exercise(
        id = "e_squat",
        name = "Bodyweight Squats",
        category = ExerciseCategory.FULL_BODY,
        targetMuscles = "Quadriceps, Glutes, Hamstrings, Core",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand with feet shoulder-width apart, toes turned slightly outwards.",
          "Hinge at your hips and bend your knees as if sitting back into a chair.",
          "Keep your chest upright and lower until thighs are parallel to the floor.",
          "Push through your midfoot and heels to return to standing."
        ),
        defaultSetsReps = "3 Sets × 15 Reps",
        restSeconds = 60,
        commonMistakes = "Knees caving inwards or heels coming off the floor.",
        beginnerModification = "Squat to a sturdy chair (box squat).",
        equipment = "Bodyweight",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+bodyweight+squats+form"
      ),
      Exercise(
        id = "e_burpees",
        name = "Low-Impact Step Burpees",
        category = ExerciseCategory.FULL_BODY,
        targetMuscles = "Full Body Conditioning, Chest, Quads, Core",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand tall with feet hip-width apart.",
          "Place hands on the floor and step one leg back, then the other into a high plank.",
          "Step feet back forward one at a time and stand upright reaching overhead.",
          "Move at a steady, rhythmic pace."
        ),
        defaultSetsReps = "3 Sets × 10 Reps",
        restSeconds = 60,
        commonMistakes = "Arching lower back in the plank position.",
        beginnerModification = "Use an elevated surface like a bench or counter.",
        equipment = "Bodyweight",
        videoUrl = "https://www.youtube.com/results?search_query=low+impact+burpee+exercise"
      ),

      // Chest & Arms
      Exercise(
        id = "e_wall_pushup",
        name = "Wall Push-ups",
        category = ExerciseCategory.CHEST_ARMS,
        targetMuscles = "Pectorals, Anterior Deltoids, Triceps",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand facing a wall about arm's length away.",
          "Place palms flat against the wall at chest height.",
          "Slowly bend elbows to bring your chest close to the wall, body aligned.",
          "Press through palms firmly to return to starting position."
        ),
        defaultSetsReps = "3 Sets × 15 Reps",
        restSeconds = 45,
        commonMistakes = "Hips sagging forward or arching lower back.",
        beginnerModification = "Step feet closer to the wall.",
        equipment = "Wall",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+wall+push+ups+form"
      ),
      Exercise(
        id = "e_incline_pushup",
        name = "Incline Push-ups",
        category = ExerciseCategory.CHEST_ARMS,
        targetMuscles = "Lower Chest, Triceps, Shoulders, Core",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Place hands on an elevated sturdy surface (bench, table, or step).",
          "Extend legs back into a straight plank position.",
          "Lower chest toward the surface with elbows at a 45-degree angle.",
          "Press back up to full arm extension."
        ),
        defaultSetsReps = "3 Sets × 10-12 Reps",
        restSeconds = 60,
        commonMistakes = "Flaring elbows out too wide at 90 degrees.",
        beginnerModification = "Use a higher surface like a table or wall.",
        equipment = "Bench / Sturdy Surface",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+incline+push+ups+form"
      ),
      Exercise(
        id = "e_doorway_rows",
        name = "Doorway Bodyweight Rows",
        category = ExerciseCategory.CHEST_ARMS,
        targetMuscles = "Upper Back, Rhomboids, Biceps, Rear Delts",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand facing a sturdy door frame with feet close to the base.",
          "Hold the frame with hands at chest height and lean back at arm's length.",
          "Pull your chest toward the frame by squeezing shoulder blades together.",
          "Lower back down smoothly under control."
        ),
        defaultSetsReps = "3 Sets × 12 Reps",
        restSeconds = 45,
        commonMistakes = "Shrugging shoulders up into the neck.",
        beginnerModification = "Stand more upright to reduce resistance angle.",
        equipment = "Door Frame",
        videoUrl = "https://www.youtube.com/results?search_query=doorway+bodyweight+rows+form"
      ),

      // Legs
      Exercise(
        id = "e_lunges",
        name = "Walking Lunges",
        category = ExerciseCategory.LEGS,
        targetMuscles = "Quads, Glutes, Hamstrings, Calves",
        difficulty = Difficulty.INTERMEDIATE,
        instructions = listOf(
          "Stand tall and step forward with one leg.",
          "Lower your back knee toward the floor until both knees bend at 90 degrees.",
          "Drive through front heel to step forward into the next lunge.",
          "Keep torso tall and core braced."
        ),
        defaultSetsReps = "3 Sets × 10 Reps per leg",
        restSeconds = 60,
        commonMistakes = "Front knee sliding past toes or slamming back knee to floor.",
        beginnerModification = "Static reverse lunges holding a wall for balance.",
        equipment = "Bodyweight",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+walking+lunges+proper+form"
      ),
      Exercise(
        id = "e_glute_bridge",
        name = "Glute Bridges",
        category = ExerciseCategory.LEGS,
        targetMuscles = "Glutes, Hamstrings, Lower Back",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Lie on your back with knees bent and feet flat on the floor.",
          "Squeeze glutes and lift hips toward the ceiling until straight from knees to shoulders.",
          "Pause for 2 seconds at the top, then lower under control.",
          "Breathe out on the ascent."
        ),
        defaultSetsReps = "3 Sets × 15 Reps",
        restSeconds = 45,
        commonMistakes = "Overarching lower back at the top.",
        beginnerModification = "Keep range of motion smaller.",
        equipment = "Mat / Floor",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+glute+bridges+form"
      ),
      Exercise(
        id = "e_calf_raises",
        name = "Standing Calf Raises",
        category = ExerciseCategory.LEGS,
        targetMuscles = "Gastrocnemius, Soleus, Ankle Stability",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand upright with balls of feet on floor or edge of a step.",
          "Press through the balls of your feet to raise your heels as high as possible.",
          "Hold contraction for 1 second, then slowly lower heels.",
          "Maintain controlled rhythm without bouncing."
        ),
        defaultSetsReps = "3 Sets × 20 Reps",
        restSeconds = 45,
        commonMistakes = "Rushing repetitions or bouncing at the bottom.",
        beginnerModification = "Perform on flat ground holding a wall for balance.",
        equipment = "Flat Floor / Step",
        videoUrl = "https://www.youtube.com/results?search_query=standing+calf+raises+proper+form"
      ),

      // Core
      Exercise(
        id = "e_plank",
        name = "Forearm Plank",
        category = ExerciseCategory.CORE,
        targetMuscles = "Rectus Abdominis, Transverse Abdominis, Obliques",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Support your body on forearms with elbows directly under shoulders.",
          "Keep legs straight and body in a single rigid line from head to heels.",
          "Engage your abs and glutes, breathing steadily.",
          "Hold without letting hips pike or sag."
        ),
        defaultSetsReps = "3 Sets × 30-45 Seconds",
        restSeconds = 60,
        commonMistakes = "Holding breath or letting hips drop.",
        beginnerModification = "Knee plank (knees resting on floor).",
        equipment = "Floor Mat",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+forearm+plank+form"
      ),
      Exercise(
        id = "e_bird_dog",
        name = "Bird Dog",
        category = ExerciseCategory.CORE,
        targetMuscles = "Erector Spinae, Core Stabilizers, Glutes",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Start on all fours with hands under shoulders and knees under hips.",
          "Simultaneously extend right arm forward and left leg straight back.",
          "Hold for 2 seconds keeping hips square to the ground.",
          "Return smoothly and switch sides."
        ),
        defaultSetsReps = "3 Sets × 10 Reps per side",
        restSeconds = 45,
        commonMistakes = "Rotating pelvis or over-arching lower back.",
        beginnerModification = "Raise only one arm or one leg at a time.",
        equipment = "Floor Mat",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+bird+dog+exercise+form"
      ),
      Exercise(
        id = "e_dead_bug",
        name = "Dead Bug",
        category = ExerciseCategory.CORE,
        targetMuscles = "Deep Core, Lower Abs, Spinal Stabilizers",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Lie on back with arms straight up and knees bent at 90 degrees.",
          "Press lower back firmly into the floor so there is zero gap.",
          "Slowly lower opposite arm and leg toward floor without arching back.",
          "Return to center and repeat on opposite side."
        ),
        defaultSetsReps = "3 Sets × 10 Reps per side",
        restSeconds = 45,
        commonMistakes = "Lower back lifting off the floor during extension.",
        beginnerModification = "Tap heels to floor with knees bent instead of extending leg.",
        equipment = "Floor Mat",
        videoUrl = "https://www.youtube.com/results?search_query=dead+bug+exercise+proper+form"
      ),

      // Mobility
      Exercise(
        id = "e_cat_cow",
        name = "Cat-Cow Spine Flow",
        category = ExerciseCategory.MOBILITY,
        targetMuscles = "Thoracic Spine, Lower Back, Neck Mobility",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Begin on hands and knees with wrists under shoulders.",
          "Inhale and drop belly toward floor, lifting chest and tailbone (Cow).",
          "Exhale and round spine upward toward ceiling, tucking chin (Cat).",
          "Move fluidly with breath for 10 gentle repetitions."
        ),
        defaultSetsReps = "2 Sets × 10 Breaths",
        restSeconds = 30,
        commonMistakes = "Forcing range of motion into pain or straining neck.",
        beginnerModification = "Perform seated in a chair.",
        equipment = "Floor Mat",
        videoUrl = "https://www.youtube.com/results?search_query=cat+cow+stretch+proper+form"
      ),
      Exercise(
        id = "e_stretching",
        name = "Hamstring & Cobra Stretch",
        category = ExerciseCategory.MOBILITY,
        targetMuscles = "Hamstrings, Hip Flexors, Pectorals",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Sit with legs extended forward and gently hinge at hips reaching for ankles.",
          "Hold for 30 seconds breathing deeply.",
          "Transition to lying prone and press palms into floor to gently lift chest into Cobra.",
          "Keep shoulders relaxed away from ears."
        ),
        defaultSetsReps = "2 Sets × 30 Seconds each",
        restSeconds = 30,
        commonMistakes = "Bouncing or locking knees painfully.",
        beginnerModification = "Use a strap or towel around feet.",
        equipment = "Floor Mat",
        videoUrl = "https://www.youtube.com/results?search_query=hamstring+stretch+and+cobra+pose+form"
      ),

      // Cardio
      Exercise(
        id = "e_jumping_jacks",
        name = "Jumping Jacks",
        category = ExerciseCategory.CARDIO,
        targetMuscles = "Cardiovascular Conditioning, Calves, Deltoids",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand tall with feet together and arms at sides.",
          "Jump feet out laterally while raising arms overhead.",
          "Jump back to starting position landing softly on balls of feet.",
          "Maintain a steady, rhythmic cadence."
        ),
        defaultSetsReps = "3 Sets × 45 Seconds",
        restSeconds = 45,
        commonMistakes = "Landing heavily on flat feet or knees caving.",
        beginnerModification = "Step jacks (step one foot out at a time without jumping).",
        equipment = "Bodyweight",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+jumping+jacks+properly"
      ),
      Exercise(
        id = "e_mountain_climber",
        name = "Mountain Climbers",
        category = ExerciseCategory.CARDIO,
        targetMuscles = "Core, Hip Flexors, Shoulders, Aerobic Endurance",
        difficulty = Difficulty.INTERMEDIATE,
        instructions = listOf(
          "Start in a solid high plank position.",
          "Drive right knee toward chest, keeping hips level.",
          "Switch legs smoothly, bringing left knee forward and extending right.",
          "Keep wrists under shoulders and core braced."
        ),
        defaultSetsReps = "3 Sets × 30 Seconds",
        restSeconds = 60,
        commonMistakes = "Hips piking high into the air.",
        beginnerModification = "Slow stepping mountain climbers.",
        equipment = "Floor Mat",
        videoUrl = "https://www.youtube.com/results?search_query=how+to+do+mountain+climbers+form"
      ),

      // Warm-up
      Exercise(
        id = "e_arm_circles",
        name = "Arm Circles & Shoulder Rolls",
        category = ExerciseCategory.WARM_UP,
        targetMuscles = "Rotator Cuff, Deltoids, Upper Trapezius",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand upright with arms extended straight out to sides at shoulder height.",
          "Make small forward circles for 15 seconds, gradually increasing diameter.",
          "Reverse direction for 15 seconds.",
          "Finish with 10 backward shoulder rolls."
        ),
        defaultSetsReps = "2 Sets × 30 Seconds",
        restSeconds = 20,
        commonMistakes = "Shrugging shoulders up toward ears.",
        beginnerModification = "Smaller circular motion.",
        equipment = "Bodyweight",
        videoUrl = "https://www.youtube.com/results?search_query=arm+circles+shoulder+warm+up+form"
      ),
      Exercise(
        id = "e_torso_twists",
        name = "Standing Torso Twists",
        category = ExerciseCategory.WARM_UP,
        targetMuscles = "Thoracic Spine, Obliques, Core Warm-up",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand with feet shoulder-width apart, knees slightly soft.",
          "Place hands on hips or bend elbows at chest height.",
          "Gently rotate torso from side to side, letting eyes follow the rotation.",
          "Keep hips relatively stable and movement smooth."
        ),
        defaultSetsReps = "2 Sets × 20 Repetitions",
        restSeconds = 20,
        commonMistakes = "Twisting too aggressively through lower back.",
        beginnerModification = "Slower, smaller rotational arc.",
        equipment = "Bodyweight",
        videoUrl = "https://www.youtube.com/results?search_query=standing+torso+twists+warm+up"
      )
    )
  }

  // --- 30-Day Beginner-Friendly Workout Plan Generator ---

  private fun generate30DayWorkouts(allEx: List<Exercise>): List<DayWorkout> {
    fun find(id: String): Exercise = allEx.find { it.id == id } ?: allEx.first()

    val plan = mutableListOf<DayWorkout>()

    for (day in 1..30) {
      val workout = when (day) {
        1 -> DayWorkout(
          day = 1,
          title = "Day 1: Full Body Foundation",
          focus = "Strength",
          isRestDay = false,
          estimatedMinutes = 20,
          exercises = listOf(find("e_arm_circles"), find("e_squat"), find("e_wall_pushup"), find("e_plank"))
        )
        2 -> DayWorkout(
          day = 2,
          title = "Day 2: Chest, Arms & Core",
          focus = "Strength",
          isRestDay = false,
          estimatedMinutes = 20,
          exercises = listOf(find("e_arm_circles"), find("e_incline_pushup"), find("e_doorway_rows"), find("e_dead_bug"))
        )
        3 -> DayWorkout(
          day = 3,
          title = "Day 3: Lower Body & Glutes",
          focus = "Strength",
          isRestDay = false,
          estimatedMinutes = 22,
          exercises = listOf(find("e_squat"), find("e_glute_bridge"), find("e_calf_raises"), find("e_stretching"))
        )
        4 -> DayWorkout(
          day = 4,
          title = "Day 4: Active Walking & Mobility",
          focus = "Recovery",
          isRestDay = true,
          estimatedMinutes = 25,
          exercises = listOf(find("e_cat_cow"), find("e_stretching"), find("e_torso_twists"))
        )
        5 -> DayWorkout(
          day = 5,
          title = "Day 5: Core & Aerobic Conditioning",
          focus = "Conditioning",
          isRestDay = false,
          estimatedMinutes = 20,
          exercises = listOf(find("e_jumping_jacks"), find("e_plank"), find("e_bird_dog"), find("e_mountain_climber"))
        )
        6 -> DayWorkout(
          day = 6,
          title = "Day 6: Full Body Tone",
          focus = "Strength",
          isRestDay = false,
          estimatedMinutes = 22,
          exercises = listOf(find("e_squat"), find("e_incline_pushup"), find("e_lunges"), find("e_dead_bug"))
        )
        7 -> DayWorkout(
          day = 7,
          title = "Day 7: Rest & Full Recovery",
          focus = "Recovery",
          isRestDay = true,
          estimatedMinutes = 15,
          exercises = listOf(find("e_cat_cow"), find("e_stretching"))
        )
        else -> {
          val cycleDay = (day - 1) % 7 + 1
          when (cycleDay) {
            1 -> DayWorkout(day, "Day $day: Full Body Strength", "Strength", false, 25, listOf(find("e_squat"), find("e_incline_pushup"), find("e_lunges"), find("e_plank")))
            2 -> DayWorkout(day, "Day $day: Upper Body & Core", "Strength", false, 25, listOf(find("e_incline_pushup"), find("e_doorway_rows"), find("e_bird_dog"), find("e_dead_bug")))
            3 -> DayWorkout(day, "Day $day: Lower Body Power", "Strength", false, 25, listOf(find("e_squat"), find("e_lunges"), find("e_glute_bridge"), find("e_calf_raises")))
            4 -> DayWorkout(day, "Day $day: Active Walk & Stretch", "Recovery", true, 30, listOf(find("e_cat_cow"), find("e_stretching")))
            5 -> DayWorkout(day, "Day $day: Core & Cardio", "Conditioning", false, 25, listOf(find("e_jumping_jacks"), find("e_plank"), find("e_mountain_climber"), find("e_glute_bridge")))
            6 -> DayWorkout(day, "Day $day: Full Body Progression", "Strength", false, 25, listOf(find("e_squat"), find("e_incline_pushup"), find("e_doorway_rows"), find("e_bird_dog")))
            else -> DayWorkout(day, "Day $day: Complete Rest & Recharge", "Recovery", true, 15, listOf(find("e_cat_cow"), find("e_stretching")))
          }
        }
      }
      plan.add(workout)
    }
    return plan
  }

  private fun getInitialMissions(): List<DailyMission> {
    val list = mutableListOf<DailyMission>()
    for (day in 1..30) {
      list.add(DailyMission("m${day}_1", day, "Execute Assigned Workout", "Complete all assigned exercises for Day $day.", MissionCategory.PHYSICAL, 50, false))
      list.add(DailyMission("m${day}_2", day, "Hit 8,000 Steps Target", "Walk briskly throughout the day to reach your step goal.", MissionCategory.WALKING, 40, false))
      list.add(DailyMission("m${day}_3", day, "Daily Hydration Target", "Drink 3 liters of clean water today.", MissionCategory.DIET, 30, false))
      list.add(DailyMission("m${day}_4", day, "10-Minute Mobility & Stretch", "Perform spinal mobility and posture stretches.", MissionCategory.LIFESTYLE, 30, false))
    }
    return list
  }
}
