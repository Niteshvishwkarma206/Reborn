package com.example.data.model

enum class MissionCategory(val label: String, val badgeColor: Long) {
  PHYSICAL("Physical Fitness", 0xFF00FF87),
  MENTAL("Mental Strength", 0xFFA855F7),
  DIET("Diet & Nutrition", 0xFF00F0FF),
  WALKING("Walk & Run", 0xFFFFB703),
  PERSONALITY("Personality", 0xFFFF2E63),
  LIFESTYLE("Lifestyle & Recovery", 0xFF38BDF8)
}

data class DailyMission(
  val id: String,
  val day: Int,
  val title: String,
  val description: String,
  val category: MissionCategory,
  val xpReward: Int,
  val isCompleted: Boolean = false,
  val completedAt: Long? = null
)

enum class ExerciseCategory(val displayName: String) {
  CHEST("Chest"),
  BACK("Back"),
  SHOULDERS("Shoulders"),
  ARMS("Arms"),
  LEGS("Legs"),
  CORE("Core"),
  FULL_BODY("Full Body"),
  CARDIO("Cardio"),
  MOBILITY("Mobility")
}

enum class Difficulty {
  BEGINNER,
  INTERMEDIATE,
  ADVANCED
}

data class Exercise(
  val id: String,
  val name: String,
  val category: ExerciseCategory,
  val targetMuscles: String,
  val difficulty: Difficulty,
  val instructions: List<String>,
  val defaultSetsReps: String,
  val restSeconds: Int,
  val commonMistakes: String,
  val beginnerModification: String,
  val equipment: String = "Bodyweight / None"
)

data class WorkoutSet(
  val setNumber: Int,
  val targetReps: String,
  val completedReps: Int = 0,
  val weightKg: Float = 0f,
  val isDone: Boolean = false
)

data class ActiveWorkoutExercise(
  val exercise: Exercise,
  val sets: List<WorkoutSet>
)

data class DayWorkoutSchedule(
  val dayName: String,
  val focusArea: String,
  val exerciseNames: List<String>,
  val estimatedMinutes: Int
)

data class WalkingActivity(
  val id: String,
  val date: String,
  val activityType: String, // "Walking" or "Running"
  val distanceKm: Float,
  val durationMinutes: Int,
  val steps: Int,
  val notes: String = ""
)

data class MealItem(
  val name: String,
  val calories: Int = 0,
  val proteinGrams: Float = 0f
)

data class NutritionEntry(
  val date: String,
  val breakfast: List<MealItem> = emptyList(),
  val lunch: List<MealItem> = emptyList(),
  val eveningSnack: List<MealItem> = emptyList(),
  val dinner: List<MealItem> = emptyList(),
  val waterMl: Int = 0,
  val notes: String = ""
)

data class MindLog(
  val date: String,
  val mood: String = "Focused",
  val focusScore: Int = 7, // 1 to 10
  val confidenceScore: Int = 7, // 1 to 10
  val meditationMinutes: Int = 0,
  val readingPages: Int = 0,
  val journalText: String = "",
  val reflectionText: String = ""
)

data class PersonalityTask(
  val id: String,
  val title: String,
  val domain: String, // Communication, Public Speaking, Eye Contact, Body Language, Confidence, Grooming, Social, Discipline
  val instructions: String,
  val xp: Int,
  val isCompleted: Boolean = false
)

data class AchievementBadge(
  val id: String,
  val title: String,
  val description: String,
  val iconEmoji: String,
  val xpBonus: Int,
  val isUnlocked: Boolean = false,
  val unlockedDate: String? = null
)

data class UserProfile(
  val name: String = "Shadow Hunter",
  val email: String = "hunter@projectreborn.app",
  val title: String = "Awakened Novice",
  val rankTier: String = "E-Rank",
  val level: Int = 1,
  val currentXp: Int = 180,
  val xpToNextLevel: Int = 500,
  val currentChallengeDay: Int = 3,
  val streakDays: Int = 3,
  val dailyWaterTargetMl: Int = 3500,
  val dailyStepTarget: Int = 8000,
  val dailyKmTarget: Float = 5.0f,
  val startDate: String = "Day 1 - Began Transformation"
)
