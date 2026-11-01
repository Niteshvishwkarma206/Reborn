package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
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
    } catch (e: Exception) {
      // Ignored if settings already locked or in test mode
    }
  }

  private val prefs: SharedPreferences =
    context.getSharedPreferences("reborn_user_prefs", Context.MODE_PRIVATE)

  private val scope = CoroutineScope(Dispatchers.IO)
  private var userDocListener: ListenerRegistration? = null
  private var missionsListener: ListenerRegistration? = null

  private val _userProfile = MutableStateFlow(loadUserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _missions = MutableStateFlow(loadMissions())
  val missions: StateFlow<List<DailyMission>> = _missions.asStateFlow()

  private val _exercises = MutableStateFlow(getInitialExercises())
  val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

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
    DayWorkoutSchedule("Monday", "Full Body", listOf("Bodyweight Squats", "Incline Push-ups", "Plank", "Jumping Jacks"), 30),
    DayWorkoutSchedule("Tuesday", "Walking and Core", listOf("Brisk Walking 3km", "Plank", "Bird Dog", "Mountain Climbers"), 35),
    DayWorkoutSchedule("Wednesday", "Upper Body", listOf("Wall Push-ups", "Incline Push-ups", "Arm Circles", "Doorway Rows"), 25),
    DayWorkoutSchedule("Thursday", "Walking and Mobility", listOf("Active Walk 4km", "Hamstring Stretch", "Cobra Stretch", "Cat-Cow Stretch"), 40),
    DayWorkoutSchedule("Friday", "Lower Body", listOf("Bodyweight Squats", "Lunges", "Glute Bridges", "Calf Raises"), 30),
    DayWorkoutSchedule("Saturday", "Light Full Body", listOf("Bird Dog", "Glute Bridges", "Wall Push-ups", "Light Mobility"), 25),
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

        val updated = _userProfile.value.copy(
          name = name,
          title = title,
          rankTier = rank,
          level = level,
          currentXp = currentXp,
          xpToNextLevel = xpNeeded,
          currentChallengeDay = day,
          streakDays = streak
        )
        _userProfile.value = updated
        saveUserProfile(updated)
      } else {
        // Initial user seed
        syncUserProfileToCloud(_userProfile.value, userId)
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
    missionsListener?.remove()
    missionsListener = null
  }

  // XP and Level Up Logic
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

      // Sync to Firestore under current user's UID
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

  fun uncompleteMission(missionId: String) {
    val currentMissions = _missions.value.toMutableList()
    val index = currentMissions.indexOfFirst { it.id == missionId }
    if (index != -1 && currentMissions[index].isCompleted) {
      val mission = currentMissions[index]
      currentMissions[index] = mission.copy(isCompleted = false, completedAt = null)
      _missions.value = currentMissions
      saveMissions(currentMissions)

      val uid = Firebase.auth.currentUser?.uid
      if (uid != null) {
        scope.launch {
          try {
            db.collection("users").document(uid).collection("missions").document(missionId)
              .update(
                mapOf(
                  "isCompleted" to false,
                  "completedAt" to null,
                  "updatedAt" to FieldValue.serverTimestamp()
                )
              )
          } catch (e: Exception) {
            Log.e("RebornRepo", "Failed to uncomplete mission in Firestore: ${e.message}")
          }
        }
      }
    }
  }

  fun awardXp(amount: Int) {
    var user = _userProfile.value
    var newXp = user.currentXp + amount
    var newLevel = user.level
    var xpNeeded = user.xpToNextLevel

    while (newXp >= xpNeeded) {
      newXp -= xpNeeded
      newLevel++
      xpNeeded = (xpNeeded * 1.25f).toInt()
    }

    val rank = when {
      newLevel >= 25 -> "S-Rank Sovereign"
      newLevel >= 18 -> "A-Rank Titan"
      newLevel >= 12 -> "B-Rank Shadow"
      newLevel >= 7 -> "C-Rank Iron"
      newLevel >= 4 -> "D-Rank Awakened"
      else -> "E-Rank Novice"
    }

    val updatedUser = user.copy(
      level = newLevel,
      currentXp = newXp,
      xpToNextLevel = xpNeeded,
      rankTier = rank.split(" ")[0],
      title = rank
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
    awardXp(10)

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

  fun addMealItem(mealType: String, item: MealItem) {
    val current = _todayNutrition.value
    val updated = when (mealType.lowercase()) {
      "breakfast" -> current.copy(breakfast = current.breakfast + item)
      "lunch" -> current.copy(lunch = current.lunch + item)
      "evening snack" -> current.copy(eveningSnack = current.eveningSnack + item)
      "dinner" -> current.copy(dinner = current.dinner + item)
      else -> current
    }
    _todayNutrition.value = updated
    saveNutrition(updated)
    awardXp(15)
  }

  fun removeMealItem(mealType: String, index: Int) {
    val current = _todayNutrition.value
    val updated = when (mealType.lowercase()) {
      "breakfast" -> current.copy(breakfast = current.breakfast.filterIndexed { i, _ -> i != index })
      "lunch" -> current.copy(lunch = current.lunch.filterIndexed { i, _ -> i != index })
      "evening snack" -> current.copy(eveningSnack = current.eveningSnack.filterIndexed { i, _ -> i != index })
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
    val xp = (activity.distanceKm * 20).toInt().coerceAtLeast(25)
    awardXp(xp)
    checkAchievements()

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
    awardXp(30)

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
      if (newState) {
        awardXp(task.xp)
      }
    }
  }

  fun completeWorkout(workoutSummary: String) {
    val current = listOf(workoutSummary) + _workoutHistory.value
    _workoutHistory.value = current
    saveWorkoutHistory(current)
    awardXp(100)
    checkAchievements()

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

  fun updateTargets(waterTarget: Int, stepTarget: Int, kmTarget: Float) {
    val updated = _userProfile.value.copy(
      dailyWaterTargetMl = waterTarget,
      dailyStepTarget = stepTarget,
      dailyKmTarget = kmTarget
    )
    _userProfile.value = updated
    saveUserProfile(updated)

    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      syncUserProfileToCloud(updated, uid)
    }
  }

  fun resetChallenge() {
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
    val user = _userProfile.value
    val completedCount = _missions.value.count { it.isCompleted }
    val totalKm = _walkingActivities.value.sumOf { it.distanceKm.toDouble() }.toFloat()
    val workoutCount = _workoutHistory.value.size

    fun unlock(badgeId: String) {
      val idx = badges.indexOfFirst { it.id == badgeId }
      if (idx != -1 && !badges[idx].isUnlocked) {
        badges[idx] = badges[idx].copy(
          isUnlocked = true,
          unlockedDate = "Unlocked Today"
        )
        awardXp(badges[idx].xpBonus)
      }
    }

    if (completedCount >= 1) unlock("first_awakening")
    if (user.streakDays >= 3) unlock("streak_3")
    if (user.streakDays >= 7) unlock("streak_7")
    if (workoutCount >= 1) unlock("first_workout")
    if (workoutCount >= 5) unlock("workout_5")
    if (totalKm >= 10.0f) unlock("distance_10")
    if (totalKm >= 50.0f) unlock("distance_50")
    if (user.level >= 5) unlock("level_5")

    _achievements.value = badges
    saveAchievements(badges)
  }

  // Persistence Helpers
  private fun loadUserProfile(): UserProfile {
    return UserProfile(
      name = prefs.getString("user_name", "Shadow Hunter") ?: "Shadow Hunter",
      email = prefs.getString("user_email", "hunter@projectreborn.app") ?: "hunter@projectreborn.app",
      title = prefs.getString("user_title", "Awakened Novice") ?: "Awakened Novice",
      rankTier = prefs.getString("user_rank", "E-Rank") ?: "E-Rank",
      level = prefs.getInt("user_level", 1),
      currentXp = prefs.getInt("user_current_xp", 180),
      xpToNextLevel = prefs.getInt("user_xp_needed", 500),
      currentChallengeDay = prefs.getInt("user_challenge_day", 3),
      streakDays = prefs.getInt("user_streak", 3),
      dailyWaterTargetMl = prefs.getInt("user_water_target", 3500),
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
            completedAt = if (obj.has("completedAt")) obj.optLong("completedAt") else null
          )
        )
      }
      list
    } catch (e: Exception) {
      getInitialMissions()
    }
  }

  private fun saveMissions(list: List<DailyMission>) {
    try {
      val array = JSONArray()
      for (m in list) {
        val obj = JSONObject()
        obj.put("id", m.id)
        obj.put("day", m.day)
        obj.put("title", m.title)
        obj.put("description", m.description)
        obj.put("category", m.category.name)
        obj.put("xpReward", m.xpReward)
        obj.put("isCompleted", m.isCompleted)
        m.completedAt?.let { obj.put("completedAt", it) }
        array.put(obj)
      }
      prefs.edit().putString("cached_missions", array.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun loadWalkingActivities(): List<WalkingActivity> {
    val json = prefs.getString("walking_activities", null)
    if (json.isNullOrEmpty()) {
      return listOf(
        WalkingActivity("walk_1", "Today", "Walking", 4.2f, 45, 5420, "Morning brisk walk in park"),
        WalkingActivity("walk_2", "Yesterday", "Running", 3.0f, 22, 3890, "Evening interval cardio")
      )
    }
    return try {
      val array = JSONArray(json)
      val list = mutableListOf<WalkingActivity>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          WalkingActivity(
            id = obj.getString("id"),
            date = obj.getString("date"),
            activityType = obj.getString("activityType"),
            distanceKm = obj.getDouble("distanceKm").toFloat(),
            durationMinutes = obj.getInt("durationMinutes"),
            steps = obj.getInt("steps"),
            notes = obj.optString("notes", "")
          )
        )
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  private fun saveWalkingActivities(list: List<WalkingActivity>) {
    try {
      val array = JSONArray()
      for (item in list) {
        val obj = JSONObject()
        obj.put("id", item.id)
        obj.put("date", item.date)
        obj.put("activityType", item.activityType)
        obj.put("distanceKm", item.distanceKm.toDouble())
        obj.put("durationMinutes", item.durationMinutes)
        obj.put("steps", item.steps)
        obj.put("notes", item.notes)
        array.put(obj)
      }
      prefs.edit().putString("walking_activities", array.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun loadTodayNutrition(): NutritionEntry {
    val water = prefs.getInt("today_water_ml", 2250)
    return NutritionEntry(
      date = "Today",
      breakfast = listOf(
        MealItem("Oats with Almonds & Banana", 320, 12f),
        MealItem("2 Boiled Eggs", 155, 13f)
      ),
      lunch = listOf(
        MealItem("Dal Tadka (1 Bowl)", 180, 9f),
        MealItem("2 Whole Wheat Roti", 220, 6f),
        MealItem("Curd / Dahi (100g)", 98, 4f)
      ),
      eveningSnack = listOf(
        MealItem("Roasted Chana (50g)", 185, 9.5f)
      ),
      dinner = listOf(
        MealItem("Paneer Bhurji (150g)", 260, 18f),
        MealItem("1 Multigrain Roti", 110, 3f),
        MealItem("Green Salad", 40, 1f)
      ),
      waterMl = water,
      notes = "Focused on high-protein Indian whole foods"
    )
  }

  private fun saveNutrition(entry: NutritionEntry) {
    prefs.edit().putInt("today_water_ml", entry.waterMl).apply()
  }

  private fun loadTodayMindLog(): MindLog {
    return MindLog(
      date = "Today",
      mood = prefs.getString("mind_mood", "Focused") ?: "Focused",
      focusScore = prefs.getInt("mind_focus", 8),
      confidenceScore = prefs.getInt("mind_confidence", 8),
      meditationMinutes = prefs.getInt("mind_meditation", 15),
      readingPages = prefs.getInt("mind_reading", 12),
      journalText = prefs.getString("mind_journal", "Conquered the morning workout and stayed disciplined on hydration.") ?: "",
      reflectionText = prefs.getString("mind_reflection", "Maintained firm eye contact in team briefing today.") ?: ""
    )
  }

  private fun saveMindLogToPrefs(log: MindLog) {
    prefs.edit()
      .putString("mind_mood", log.mood)
      .putInt("mind_focus", log.focusScore)
      .putInt("mind_confidence", log.confidenceScore)
      .putInt("mind_meditation", log.meditationMinutes)
      .putInt("mind_reading", log.readingPages)
      .putString("mind_journal", log.journalText)
      .putString("mind_reflection", log.reflectionText)
      .apply()
  }

  private fun loadPersonalityTasks(): List<PersonalityTask> {
    return listOf(
      PersonalityTask("p1", "Assertive Voice & Steady Pitch", "Communication", "Speak from the diaphragm. Eliminate filler words ('um', 'like').", 40, true),
      PersonalityTask("p2", "The 3-Second Eye Contact Rule", "Eye Contact", "Hold firm, friendly eye contact for 3 full seconds before blinking away.", 45, true),
      PersonalityTask("p3", "Open Alpha Posture", "Body Language", "Shoulders back, chest elevated, chin parallel to the ground. Avoid folding arms.", 35, false),
      PersonalityTask("p4", "Micro-Public Speech", "Public Speaking", "Record a 60-second video explaining a complex concept without looking at notes.", 60, false),
      PersonalityTask("p5", "Sharpened Grooming Protocol", "Grooming", "Trim beard/clean shave, style hair, moisturize, and wear ironed clean clothes.", 30, true),
      PersonalityTask("p6", "Cold Social Interaction", "Social Interaction", "Compliment a stranger genuinely or strike a 30-second conversation.", 50, false),
      PersonalityTask("p7", "Zero Sinking to Temptations", "Discipline", "Delay gratification by at least 30 minutes when facing an impulse trigger.", 40, false)
    )
  }

  private fun savePersonalityTasks(list: List<PersonalityTask>) {
    // Keep in memory & prefs flag
  }

  private fun loadAchievements(): List<AchievementBadge> {
    return listOf(
      AchievementBadge("first_awakening", "First Awakening", "Completed your very first mission protocol", "⚡", 50, true, "Day 1"),
      AchievementBadge("streak_3", "3-Day Fire Streak", "Maintained unbroken focus for 3 consecutive days", "🔥", 100, true, "Day 3"),
      AchievementBadge("streak_7", "7-Day Iron Will", "Surpassed the critical first week milestone", "🛡️", 200, false),
      AchievementBadge("first_workout", "Iron Disciple", "Completed your first structured workout session", "⚔️", 80, true, "Day 2"),
      AchievementBadge("workout_5", "Titan in the Making", "Completed 5 rigorous workout training routines", "🏆", 250, false),
      AchievementBadge("distance_10", "Road Conqueror", "Accumulated 10 kilometers of walking/running", "👟", 120, true, "Day 3"),
      AchievementBadge("distance_50", "Centurion Stride", "Covered 50 kilometers across transformation", "🏃", 500, false),
      AchievementBadge("level_5", "Awakened Rank D", "Surpassed Level 5 in player character power", "⭐", 300, false),
      AchievementBadge("discipline_30", "Reborn Sovereign", "Conquered the full 30-Day Protocol", "👑", 1000, false)
    )
  }

  private fun saveAchievements(list: List<AchievementBadge>) {
    // save
  }

  private fun loadWorkoutHistory(): List<String> {
    return listOf(
      "Completed Full Body Blast • 4 exercises • 12 sets • Day 1",
      "Completed Upper Body Protocol • 3 exercises • 9 sets • Day 2"
    )
  }

  private fun saveWorkoutHistory(list: List<String>) {
    // save
  }

  // Pre-seeded Exercises
  private fun getInitialExercises(): List<Exercise> {
    return listOf(
      Exercise(
        id = "e_wall_pushup",
        name = "Wall Push-ups",
        category = ExerciseCategory.CHEST,
        targetMuscles = "Pectorals, Anterior Deltoids, Triceps",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand facing a wall approximately an arm's length away with feet shoulder-width apart.",
          "Place your palms flat against the wall at chest height, slightly wider than shoulder-width.",
          "Slowly bend your elbows to lower your chest toward the wall while keeping your core braced and body in a straight plank line.",
          "Push firmly through your palms to return to the starting position."
        ),
        defaultSetsReps = "3 Sets × 12-15 Reps",
        restSeconds = 45,
        commonMistakes = "Arching the lower back or letting the hips sag toward the wall.",
        beginnerModification = "Step feet closer to the wall to decrease the angle and load.",
        equipment = "Wall"
      ),
      Exercise(
        id = "e_incline_pushup",
        name = "Incline Push-ups",
        category = ExerciseCategory.CHEST,
        targetMuscles = "Lower Chest, Triceps, Shoulders, Core",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Place hands on an elevated sturdy surface (bench, table, or step).",
          "Extend legs back into a straight push-up plank with toes anchored.",
          "Lower your chest down until it gently taps the elevated edge.",
          "Press forcefully back to full elbow extension."
        ),
        defaultSetsReps = "3 Sets × 10-12 Reps",
        restSeconds = 60,
        commonMistakes = "Flaring elbows out at 90 degrees instead of tucking at 45 degrees.",
        beginnerModification = "Use a higher surface like a kitchen counter or desk.",
        equipment = "Sturdy Bench / Counter"
      ),
      Exercise(
        id = "e_squat",
        name = "Bodyweight Squats",
        category = ExerciseCategory.LEGS,
        targetMuscles = "Quadriceps, Glutes, Hamstrings, Core",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand with feet shoulder-width apart, toes turned slightly outwards.",
          "Initiate the movement by sending your hips back and down as if sitting in an invisible chair.",
          "Keep your chest upright, knees tracking over the second toes, and lower to parallel.",
          "Drive firmly through mid-foot and heels to return to standing lockout."
        ),
        defaultSetsReps = "3 Sets × 15-20 Reps",
        restSeconds = 60,
        commonMistakes = "Knees caving inwards (valgus) or heels lifting off the floor.",
        beginnerModification = "Perform squats onto a chair (box squats) to ensure depth control.",
        equipment = "Bodyweight"
      ),
      Exercise(
        id = "e_lunges",
        name = "Walking / Alternating Lunges",
        category = ExerciseCategory.LEGS,
        targetMuscles = "Quads, Gluteus Maximus, Calves, Balance",
        difficulty = Difficulty.INTERMEDIATE,
        instructions = listOf(
          "Stand tall with hands on hips or clasped at your chest.",
          "Step forward about 2-3 feet with your lead leg and lower your rear knee toward the floor.",
          "Both knees should bend to approximately 90-degree angles.",
          "Drive through your front heel to step back or step through to the next lunge."
        ),
        defaultSetsReps = "3 Sets × 10-12 Reps per leg",
        restSeconds = 60,
        commonMistakes = "Front knee projecting drastically beyond toes or slamming back knee.",
        beginnerModification = "Reverse lunges or hold onto a wall for stability.",
        equipment = "Bodyweight"
      ),
      Exercise(
        id = "e_glute_bridge",
        name = "Glute Bridges",
        category = ExerciseCategory.LEGS,
        targetMuscles = "Glutes, Posterior Chain, Hamstrings, Lower Back",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Lie flat on your back with knees bent and feet flat on the floor, hip-width apart.",
          "Rest arms by your sides with palms pressing down.",
          "Squeeze your glutes tightly and lift your hips toward the ceiling until knees, hips, and shoulders form a line.",
          "Hold the contraction for 2 seconds at the top, then lower under control."
        ),
        defaultSetsReps = "3 Sets × 15 Reps",
        restSeconds = 45,
        commonMistakes = "Hyperextending the lower back rather than driving from glutes.",
        beginnerModification = "Place hands under hips for sensory feedback and support.",
        equipment = "Floor Mat"
      ),
      Exercise(
        id = "e_plank",
        name = "Forearm Plank",
        category = ExerciseCategory.CORE,
        targetMuscles = "Rectus Abdominis, Transverse Abdominis, Obliques, Shoulders",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Lie face down, then prop yourself up on forearms with elbows directly under shoulders.",
          "Step feet back so toes support lower body; keep legs straight and glutes engaged.",
          "Create maximum tension through your abs, breathing rhythmically without sagging hips.",
          "Hold the rigid plank position for the target duration."
        ),
        defaultSetsReps = "3 Sets × 30-45 Seconds",
        restSeconds = 60,
        commonMistakes = "Holding your breath or allowing hips to pike too high or sag low.",
        beginnerModification = "Knee plank (drop knees to floor while keeping torso braced).",
        equipment = "Bodyweight"
      ),
      Exercise(
        id = "e_bird_dog",
        name = "Bird Dog",
        category = ExerciseCategory.CORE,
        targetMuscles = "Erector Spinae, Core Stabilizers, Glutes, Deltoids",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Start on all fours with hands under shoulders and knees under hips (tabletop).",
          "Simultaneously reach your right arm straight forward and left leg straight back.",
          "Keep hips square to the ground without rotating the pelvis.",
          "Pause for 2 seconds, return smoothly to tabletop, and alternate sides."
        ),
        defaultSetsReps = "3 Sets × 10 Reps per side",
        restSeconds = 45,
        commonMistakes = "Over-arching the back or kicking the leg too high above the spine.",
        beginnerModification = "Raise only arms or only legs individually first.",
        equipment = "Floor Mat"
      ),
      Exercise(
        id = "e_jumping_jacks",
        name = "Jumping Jacks",
        category = ExerciseCategory.CARDIO,
        targetMuscles = "Cardiovascular Endurance, Calves, Deltoids",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Stand upright with feet together and arms resting by your sides.",
          "Jump your feet out laterally while simultaneously raising arms overhead.",
          "Quickly reverse the movement by jumping back to start with arms at your sides.",
          "Land softly on the balls of your feet in a fluid rhythm."
        ),
        defaultSetsReps = "3 Sets × 45 Seconds",
        restSeconds = 45,
        commonMistakes = "Landing heavily on heels or letting knees bow inwards.",
        beginnerModification = "Step jacks: step one foot out at a time without jumping.",
        equipment = "Bodyweight"
      ),
      Exercise(
        id = "e_mountain_climber",
        name = "Mountain Climbers",
        category = ExerciseCategory.CARDIO,
        targetMuscles = "Core, Hip Flexors, Deltoids, Aerobic System",
        difficulty = Difficulty.INTERMEDIATE,
        instructions = listOf(
          "Assume a solid high plank position with hands under shoulders.",
          "Drive your right knee forward toward your chest, engaging abs.",
          "Quickly switch legs, driving the left knee forward while extending the right back.",
          "Maintain a steady, controlled pace without bouncing hips up and down."
        ),
        defaultSetsReps = "3 Sets × 30 Seconds",
        restSeconds = 60,
        commonMistakes = "Hips riding up into a downward dog posture.",
        beginnerModification = "Perform at a slow walking tempo rather than running speed.",
        equipment = "Floor Mat"
      ),
      Exercise(
        id = "e_stretching",
        name = "Hamstring & Cobra Mobility Flow",
        category = ExerciseCategory.MOBILITY,
        targetMuscles = "Hamstrings, Hip Flexors, Lower Back, Chest",
        difficulty = Difficulty.BEGINNER,
        instructions = listOf(
          "Sit on the floor with legs extended forward. Hinge from the hips to reach toward toes.",
          "Hold the gentle hamstring stretch for 30 seconds without bouncing.",
          "Transition to lying prone and press palms into floor to lift chest into gentle Cobra stretch.",
          "Breathe deeply into the abdomen to release tension across hip flexors."
        ),
        defaultSetsReps = "2 Sets × 45 Seconds per pose",
        restSeconds = 30,
        commonMistakes = "Forcing depth and straining joints instead of easing with exhales.",
        beginnerModification = "Use a strap or towel looped around feet for hamstrings.",
        equipment = "Floor Mat"
      )
    )
  }

  // Pre-seeded 30-Day Missions
  private fun getInitialMissions(): List<DailyMission> {
    val list = mutableListOf<DailyMission>()
    for (day in 1..30) {
      when (day) {
        1 -> {
          list.add(DailyMission("m1_1", 1, "The Awakening: 3km Walk", "Kick off Day 1 with a dedicated 3-kilometer brisk walk. No excuses.", MissionCategory.WALKING, 50, true, 1698765432000L))
          list.add(DailyMission("m1_2", 1, "Hydration Foundation: 3 Liters", "Consume at least 3,000ml of clean water throughout the day.", MissionCategory.DIET, 40, true, 1698765432000L))
          list.add(DailyMission("m1_3", 1, "Chest & Core Ignition", "Complete 3 sets of Wall or Incline Push-ups and 30s Plank.", MissionCategory.PHYSICAL, 60, true, 1698765432000L))
          list.add(DailyMission("m1_4", 1, "10-Min Silent Meditation", "Sit in total stillness. Focus purely on breath rhythm.", MissionCategory.MENTAL, 40, true, 1698765432000L))
          list.add(DailyMission("m1_5", 1, "Commanding Eye Contact", "Maintain uninterrupted eye contact for 3 seconds in all interactions.", MissionCategory.PERSONALITY, 45, false))
        }
        2 -> {
          list.add(DailyMission("m2_1", 2, "Upper Body Mastery", "Perform 3 sets of Incline Push-ups and Arm Circles.", MissionCategory.PHYSICAL, 50, true, 1698851832000L))
          list.add(DailyMission("m2_2", 2, "Zero Refined Sugar Day", "Eliminate soda, sugary tea/coffee, and sweets today.", MissionCategory.DIET, 50, true, 1698851832000L))
          list.add(DailyMission("m2_3", 2, "4km Aerobic Stride", "Cover 4km walking or light jogging at a steady pace.", MissionCategory.WALKING, 55, false))
          list.add(DailyMission("m2_4", 2, "Mental Reflection Journal", "Write down 3 mental obstacles you conquered yesterday.", MissionCategory.MENTAL, 40, true, 1698851832000L))
          list.add(DailyMission("m2_5", 2, "Posture Realignment", "Check posture hourly. Shoulders retracted, chest high.", MissionCategory.PERSONALITY, 35, false))
        }
        3 -> {
          list.add(DailyMission("m3_1", 3, "Leg Power: 45 Squats", "Complete 3 sets of 15 bodyweight squats with crisp form.", MissionCategory.PHYSICAL, 60, false))
          list.add(DailyMission("m3_2", 3, "High-Protein Indian Fuel", "Eat at least 2 high-protein items (Paneer, Dal, Eggs, Chana).", MissionCategory.DIET, 45, true, 1698938232000L))
          list.add(DailyMission("m3_3", 3, "Brisk 5km March", "Complete a brisk 5-kilometer walking session.", MissionCategory.WALKING, 70, false))
          list.add(DailyMission("m3_4", 3, "15 Pages Deep Reading", "Read 15 pages of non-fiction, philosophy, or personal growth.", MissionCategory.MENTAL, 45, false))
          list.add(DailyMission("m3_5", 3, "Clear Vocal Tone", "Speak in full complete sentences with zero mumbling today.", MissionCategory.PERSONALITY, 40, false))
        }
        7 -> {
          list.add(DailyMission("m7_1", 7, "Milestone Boss 1: Week 1 Titan", "Complete a 30-minute full body workout circuit.", MissionCategory.PHYSICAL, 100, false))
          list.add(DailyMission("m7_2", 7, "6km Endurance Stride", "Clock 6 kilometers in a single outdoor session.", MissionCategory.WALKING, 80, false))
          list.add(DailyMission("m7_3", 7, "Week 1 Reflection Dossier", "Review your Day 1 vs Day 7 energy and confidence levels.", MissionCategory.MENTAL, 60, false))
          list.add(DailyMission("m7_4", 7, "Grooming & Wardrobe Audit", "Complete a full grooming routine and prepare sharp attire.", MissionCategory.PERSONALITY, 50, false))
        }
        14 -> {
          list.add(DailyMission("m14_1", 14, "Milestone Boss 2: Iron Will (Halfway)", "Conquer 50 pushups and 50 squats total throughout the day.", MissionCategory.PHYSICAL, 150, false))
          list.add(DailyMission("m14_2", 14, "7km Distance Benchmark", "Push past comfort zones with a 7km walking/running session.", MissionCategory.WALKING, 100, false))
          list.add(DailyMission("m14_3", 14, "20-Min Unbroken Meditation", "Deep alpha-state meditation with controlled breathing.", MissionCategory.MENTAL, 80, false))
        }
        21 -> {
          list.add(DailyMission("m21_1", 21, "Milestone Boss 3: Habit Transformation", "Full 45-minute strength & cardio routine.", MissionCategory.PHYSICAL, 200, false))
          list.add(DailyMission("m21_2", 21, "8km Distance Mastery", "Complete 8 kilometers on foot.", MissionCategory.WALKING, 120, false))
          list.add(DailyMission("m21_3", 21, "Public Speaking Challenge", "Deliver a 2-minute impromptu speech on camera.", MissionCategory.PERSONALITY, 100, false))
        }
        30 -> {
          list.add(DailyMission("m30_1", 30, "The Final Sovereign Ascent: 10km Run/Walk", "Complete the monumental 10km distance challenge.", MissionCategory.WALKING, 300, false))
          list.add(DailyMission("m30_2", 30, "Day 30 Reborn Transformation Audit", "Compare Day 1 vs Day 30 metrics, photos, and confidence.", MissionCategory.LIFESTYLE, 250, false))
          list.add(DailyMission("m30_3", 30, "Ascend to Sovereign Rank", "Final transformation checklist and permanent habit pledge.", MissionCategory.MENTAL, 300, false))
        }
        else -> {
          list.add(DailyMission("m${day}_1", day, "Physical Protocol Day $day", "Execute scheduled daily workout exercises and core training.", MissionCategory.PHYSICAL, 50, false))
          list.add(DailyMission("m${day}_2", day, "Daily 5km Walking Objective", "Clock 5km total distance across daily strides.", MissionCategory.WALKING, 60, false))
          list.add(DailyMission("m${day}_3", day, "Clean Nutrition & Hydration", "Hit 3.5L water and optimal protein intake.", MissionCategory.DIET, 40, false))
          list.add(DailyMission("m${day}_4", day, "Mental Fortress: 15-min Focus", "Mindfulness and evening journaling review.", MissionCategory.MENTAL, 40, false))
          list.add(DailyMission("m${day}_5", day, "Personality Mastery Task", "Execute social presence, grooming, or communication drill.", MissionCategory.PERSONALITY, 45, false))
        }
      }
    }
    return list
  }
}
