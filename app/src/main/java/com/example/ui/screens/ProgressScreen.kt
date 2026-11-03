package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DailyMission
import com.example.data.model.DailyWorkoutProgress
import com.example.data.model.UserProfile
import com.example.ui.components.CircularWorkoutProgress
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun ProgressScreen(
  viewModel: RebornViewModel,
  user: UserProfile,
  missions: List<DailyMission> = emptyList()
) {
  val workoutProgressMap by viewModel.dailyWorkoutProgress.collectAsStateWithLifecycle()
  val currentChallengeDay = user.currentChallengeDay
  var inspectedDay by remember { mutableStateOf(currentChallengeDay) }

  val todayProgress = workoutProgressMap[currentChallengeDay] ?: remember(currentChallengeDay) {
    viewModel.getWorkoutProgressForDay(currentChallengeDay)
  }
  val todayWorkout = remember(currentChallengeDay) { viewModel.getWorkoutForDay(currentChallengeDay) }

  // Overall plan progress based on completed days
  val completedDaysCount = (1..30).count { d ->
    val prog = workoutProgressMap[d] ?: viewModel.getWorkoutProgressForDay(d)
    prog.isCompleted
  }
  val planCompletionPct = (completedDaysCount * 100) / 30

  val inspectedWorkout = remember(inspectedDay) { viewModel.getWorkoutForDay(inspectedDay) }
  val inspectedProgress = workoutProgressMap[inspectedDay] ?: remember(inspectedDay) {
    viewModel.getWorkoutProgressForDay(inspectedDay)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CleanBackground)
      .testTag("progress_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      RebornTopHeader(
        title = "Transformation Progress",
        subtitle = "30-Day workout tracking & real-time analytics"
      )
    }

    // 1. Overall Plan Progress Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_overall_plan_progress"),
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
                text = "OVERALL PLAN COMPLETION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CleanPrimary,
                letterSpacing = 0.8.sp
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "$planCompletionPct%",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
              )
              Text(
                text = "$completedDaysCount of 30 days completed",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
              )
            }

            CircularWorkoutProgress(
              percentage = planCompletionPct,
              size = 68.dp,
              strokeWidth = 7.dp,
              label = "Plan"
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          LinearProgressIndicator(
            progress = { (planCompletionPct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = CleanPrimary,
            trackColor = Color(0xFFE2E8F0)
          )
        }
      }
    }

    // 2. Today's Workout Completion Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_today_workout_completion"),
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
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "TODAY'S WORKOUT PROGRESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.6.sp
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Day $currentChallengeDay: ${todayWorkout.title}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = if (todayWorkout.isRestDay) "Assigned Recovery Day"
                else "${todayProgress.completedExercises} of ${todayProgress.totalExercises} exercises completed",
                fontSize = 13.sp,
                color = TextSecondary
              )
            }

            CircularWorkoutProgress(
              percentage = todayProgress.percentage,
              size = 64.dp,
              strokeWidth = 6.dp,
              label = if (todayProgress.isCompleted) "Done" else "${todayProgress.percentage}%"
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          LinearProgressIndicator(
            progress = { (todayProgress.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = CleanPrimary,
            trackColor = Color(0xFFE2E8F0)
          )
        }
      }
    }

    // 3. 30-Day Transformation Progress Chart
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_30_day_chart"),
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
                text = "30-DAY PROGRESS CHART",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CleanPrimary,
                letterSpacing = 0.8.sp
              )
              Text(
                text = "Tap any day to view workout details",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 30-Day Chart: 6 rows of 5 days or 5 rows of 6 days grid
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..30).chunked(6).forEach { weekChunk ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                weekChunk.forEach { dayNumber ->
                  val dayProg = workoutProgressMap[dayNumber] ?: viewModel.getWorkoutProgressForDay(dayNumber)
                  val dayWk = viewModel.getWorkoutForDay(dayNumber)
                  val isSelected = dayNumber == inspectedDay

                  DayChartPoint(
                    dayNumber = dayNumber,
                    progress = dayProg,
                    isRestDay = dayWk.isRestDay,
                    isSelected = isSelected,
                    onClick = { inspectedDay = dayNumber }
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Chart Legend
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            LegendItem(color = CleanPrimary, label = "Completed (100%)")
            LegendItem(color = Color(0xFF6EE7B7), label = "In Progress")
            LegendItem(color = Color(0xFFBAE6FD), label = "Rest Day")
            LegendItem(color = Color(0xFFE2E8F0), label = "Pending")
          }
        }
      }
    }

    // 4. Inspected Day Workout Details Card (Interactive inspection when a day is tapped)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_inspected_day_detail"),
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
                text = "DAY $inspectedDay DETAILS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CleanPrimary,
                letterSpacing = 0.6.sp
              )
              Text(
                text = inspectedWorkout.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = if (inspectedWorkout.isRestDay) "Active Recovery" else "${inspectedWorkout.focus} • ${inspectedWorkout.estimatedMinutes} mins",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (inspectedProgress.isCompleted) CleanPrimaryLight else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (inspectedProgress.isCompleted) CleanPrimary else CleanBorder)
            ) {
              Text(
                text = if (inspectedWorkout.isRestDay) "Rest Day" else "${inspectedProgress.percentage}% Done",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (inspectedProgress.isCompleted) CleanPrimaryDark else TextSecondary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (inspectedWorkout.isRestDay) {
            Text(
              text = "No workout assigned for Day $inspectedDay. Rest, light walking, and mobility stretch are encouraged to allow systemic recovery.",
              fontSize = 13.sp,
              color = TextSecondary,
              lineHeight = 18.sp
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              inspectedWorkout.exercises.forEach { ex ->
                val isDone = inspectedProgress.completedExerciseIds.contains(ex.id)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.toggleExerciseInWorkout(inspectedDay, ex.id) }
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Checkbox(
                    checked = isDone,
                    onCheckedChange = { viewModel.toggleExerciseInWorkout(inspectedDay, ex.id) },
                    colors = CheckboxDefaults.colors(
                      checkedColor = CleanPrimary,
                      uncheckedColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = ex.name,
                      fontSize = 13.sp,
                      fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                      color = if (isDone) TextSecondary else TextPrimary
                    )
                    Text(
                      text = "${ex.defaultSetsReps} • Target: ${ex.targetMuscles}",
                      fontSize = 11.sp,
                      color = TextSecondary
                    )
                  }
                  Text(
                    text = if (isDone) "Done" else "Pending",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) CleanPrimary else TextSecondary
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DayChartPoint(
  dayNumber: Int,
  progress: DailyWorkoutProgress,
  isRestDay: Boolean,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val pct = progress.percentage
  val isCompleted = progress.isCompleted

  val pointBg = when {
    isCompleted -> CleanPrimary
    pct > 0 -> Color(0xFF6EE7B7)
    isRestDay -> Color(0xFFBAE6FD)
    else -> Color(0xFFE2E8F0)
  }

  val textColor = when {
    isCompleted -> Color.White
    pct > 0 -> TextPrimary
    isRestDay -> Color(0xFF0369A1)
    else -> TextSecondary
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(50.dp)
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) CleanPrimaryDark else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
      )
      .padding(vertical = 4.dp)
      .testTag("chart_day_$dayNumber")
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(pointBg),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = if (isRestDay && pct == 0) "R" else if (pct == 100) "✓" else if (pct > 0) "$pct%" else "$dayNumber",
        fontSize = if (pct in 1..99) 9.sp else 11.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = "D$dayNumber",
      fontSize = 10.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) CleanPrimary else TextSecondary
    )
  }
}

@Composable
private fun LegendItem(
  color: Color,
  label: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      fontSize = 10.sp,
      color = TextSecondary,
      fontWeight = FontWeight.Medium
    )
  }
}
