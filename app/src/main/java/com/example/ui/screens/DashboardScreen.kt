package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DailyHabit
import com.example.data.model.DailyMission
import com.example.data.model.StepSource
import com.example.data.model.UserProfile
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun DashboardScreen(
  viewModel: RebornViewModel,
  user: UserProfile,
  missions: List<DailyMission>,
  onNavigate: (AppScreen) -> Unit
) {
  val stepState by viewModel.stepTrackerState.collectAsStateWithLifecycle()
  val habits by viewModel.dailyHabits.collectAsStateWithLifecycle()
  val nutrition by viewModel.todayNutrition.collectAsStateWithLifecycle()
  val workoutProgressMap by viewModel.dailyWorkoutProgress.collectAsStateWithLifecycle()

  val currentDay = user.currentChallengeDay
  val todayWorkout = remember(currentDay) { viewModel.getWorkoutForDay(currentDay) }
  val todayProgress = workoutProgressMap[currentDay] ?: remember(currentDay) {
    viewModel.getWorkoutProgressForDay(currentDay)
  }

  // Calculate plan completion based on completed days
  val completedDaysCount = (1..30).count { d ->
    val prog = workoutProgressMap[d] ?: viewModel.getWorkoutProgressForDay(d)
    prog.isCompleted
  }
  val planCompletionPercentage = (completedDaysCount * 100) / 30

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CleanBackground)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Bar Header
    item {
      RebornTopHeader(
        title = "reborn",
        subtitle = "Clean fitness & daily transformation",
        streakDays = user.streakDays
      )
    }

    // 1. Day X of 30 & Overall Plan Completion
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_plan_progress"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "30-DAY TRANSFORMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CleanPrimary,
                letterSpacing = 0.8.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Day $currentDay of 30",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(CleanPrimaryLight)
                .border(2.dp, CleanPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "$planCompletionPercentage%",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CleanPrimaryDark
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          LinearProgressIndicator(
            progress = { (planCompletionPercentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = CleanPrimary,
            trackColor = Color(0xFFE2E8F0)
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "$completedDaysCount of 30 days completed",
              fontSize = 12.sp,
              color = TextSecondary,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "View Chart",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = CleanPrimary,
              modifier = Modifier
                .clickable { onNavigate(AppScreen.PROGRESS) }
                .testTag("btn_view_progress_chart")
            )
          }
        }
      }
    }

    // 2. Today's Workout Card with Circular Progress & Start Workout
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_today_workout"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "TODAY'S WORKOUT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.6.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = todayWorkout.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (todayWorkout.isRestDay) "Active Recovery & Mobility Day"
                else "${todayWorkout.focus} • ${todayWorkout.estimatedMinutes} min • ${todayWorkout.exercises.size} exercises",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            CircularWorkoutProgress(
              percentage = todayProgress.percentage,
              size = 64.dp,
              strokeWidth = 6.dp,
              label = if (todayProgress.isCompleted) "Done" else "${todayProgress.completedExercises}/${todayProgress.totalExercises}"
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Exercises checklist preview
          if (todayWorkout.exercises.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              todayWorkout.exercises.take(3).forEach { ex ->
                val isDone = todayProgress.completedExerciseIds.contains(ex.id)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.toggleExerciseInWorkout(currentDay, ex.id) }
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Checkbox(
                    checked = isDone,
                    onCheckedChange = { viewModel.toggleExerciseInWorkout(currentDay, ex.id) },
                    colors = CheckboxDefaults.colors(
                      checkedColor = CleanPrimary,
                      uncheckedColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = ex.name,
                    fontSize = 13.sp,
                    color = if (isDone) TextSecondary else TextPrimary,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium
                  )
                  Spacer(modifier = Modifier.weight(1f))
                  Text(
                    text = ex.defaultSetsReps,
                    fontSize = 11.sp,
                    color = TextSecondary
                  )
                }
              }
              if (todayWorkout.exercises.size > 3) {
                Text(
                  text = "+ ${todayWorkout.exercises.size - 3} more exercises",
                  fontSize = 11.sp,
                  color = TextSecondary,
                  modifier = Modifier.padding(start = 34.dp, top = 2.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.startWorkoutForDay(currentDay)
              },
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("btn_start_workout"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
            ) {
              Icon(
                imageVector = if (todayProgress.isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (todayProgress.isCompleted) "Workout Finished" else "Start Workout",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }

            OutlinedButton(
              onClick = { onNavigate(AppScreen.PLAN) },
              modifier = Modifier
                .height(44.dp)
                .testTag("btn_view_full_plan"),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, CleanBorder)
            ) {
              Text("Plan", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
          }
        }
      }
    }

    // 3. Steps Today, Distance Walked & Daily Step Goal
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onNavigate(AppScreen.ACTIVITY) }
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_step_tracker"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(CleanEmeraldLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.DirectionsWalk,
                  contentDescription = null,
                  tint = CleanPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "DAILY STEPS",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextSecondary,
                  letterSpacing = 0.6.sp
                )
                Text(
                  text = "${stepState.todaySteps} / ${stepState.dailyGoal}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
            }

            // Source Badge
            val sourceText = when (stepState.source) {
              StepSource.HEALTH_CONNECT -> "Health Connect"
              StepSource.MANUAL -> "Manual"
              StepSource.NONE -> "No Data"
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (stepState.source == StepSource.HEALTH_CONNECT) CleanPrimaryLight else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (stepState.source == StepSource.HEALTH_CONNECT) CleanPrimary else Color(0xFFCBD5E1))
            ) {
              Text(
                text = sourceText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (stepState.source == StepSource.HEALTH_CONNECT) CleanPrimaryDark else TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          val stepProgress = if (stepState.dailyGoal > 0) {
            (stepState.todaySteps.toFloat() / stepState.dailyGoal).coerceIn(0f, 1f)
          } else 0f

          LinearProgressIndicator(
            progress = { stepProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = CleanPrimary,
            trackColor = Color(0xFFE2E8F0)
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Estimated: ${String.format("%.2f", stepState.distanceKm)} km",
              fontSize = 12.sp,
              color = TextSecondary,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Activity Log →",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = CleanPrimary
            )
          }
        }
      }
    }

    // 4. Water Intake
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_water_intake"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.WaterDrop,
                  contentDescription = null,
                  tint = CleanBlue,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "WATER INTAKE",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextSecondary,
                  letterSpacing = 0.6.sp
                )
                Text(
                  text = "${nutrition.waterMl} / ${user.dailyWaterTargetMl} ml",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
            }

            Text(
              text = "${((nutrition.waterMl * 100f) / user.dailyWaterTargetMl).toInt().coerceAtMost(100)}%",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = CleanBlue
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          val waterProgress = if (user.dailyWaterTargetMl > 0) {
            (nutrition.waterMl.toFloat() / user.dailyWaterTargetMl).coerceIn(0f, 1f)
          } else 0f

          LinearProgressIndicator(
            progress = { waterProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = CleanBlue,
            trackColor = Color(0xFFE2E8F0)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.addWater(250) },
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .testTag("btn_add_water_250"),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
              contentPadding = PaddingValues(0.dp)
            ) {
              Text("+250 ml", fontSize = 12.sp, color = CleanBlue, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { viewModel.addWater(500) },
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .testTag("btn_add_water_500"),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
              contentPadding = PaddingValues(0.dp)
            ) {
              Text("+500 ml", fontSize = 12.sp, color = CleanBlue, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { viewModel.addWater(-250) },
              modifier = Modifier
                .weight(0.8f)
                .height(36.dp)
                .testTag("btn_remove_water_250"),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, CleanBorder),
              contentPadding = PaddingValues(0.dp)
            ) {
              Text("-250 ml", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
          }
        }
      }
    }

    // 5. Today's Habits Checklist
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_daily_habits"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TODAY'S HABITS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.6.sp
              )
              Text(
                text = "Daily Foundation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            val completedHabitsCount = habits.count { it.isCompleted }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (completedHabitsCount == habits.size) CleanPrimaryLight else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (completedHabitsCount == habits.size) CleanPrimary else CleanBorder)
            ) {
              Text(
                text = "$completedHabitsCount / ${habits.size} Done",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (completedHabitsCount == habits.size) CleanPrimaryDark else TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            habits.forEach { habit ->
              HabitCheckRow(
                habit = habit,
                onToggle = { viewModel.toggleHabit(habit.id) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HabitCheckRow(
  habit: DailyHabit,
  onToggle: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable { onToggle() }
      .background(if (habit.isCompleted) Color(0xFFF8FAFC) else Color.Transparent)
      .padding(horizontal = 4.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = habit.isCompleted,
      onCheckedChange = { onToggle() },
      colors = CheckboxDefaults.colors(
        checkedColor = CleanPrimary,
        uncheckedColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier
        .size(24.dp)
        .testTag("checkbox_habit_${habit.id}")
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = habit.title,
        fontSize = 14.sp,
        fontWeight = if (habit.isCompleted) FontWeight.Normal else FontWeight.Medium,
        color = if (habit.isCompleted) TextSecondary else TextPrimary,
        textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None
      )
      Text(
        text = habit.category,
        fontSize = 11.sp,
        color = TextSecondary
      )
    }
  }
}
