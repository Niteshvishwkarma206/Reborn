package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayWorkoutSchedule
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveWorkoutSession
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun WorkoutScreen(
  viewModel: RebornViewModel,
  activeWorkout: ActiveWorkoutSession?,
  restSeconds: Int,
  weeklySchedule: List<DayWorkoutSchedule>,
  workoutHistory: List<String>
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "WORKOUT PROTOCOL",
        subtitle = "WEEKLY SCHEDULE & REAL-TIME TRAINING HUD"
      )
    }

    // Active Workout Live HUD
    if (activeWorkout != null) {
      item {
        ActiveWorkoutSessionCard(
          session = activeWorkout,
          restSeconds = restSeconds,
          onCompleteSet = { exIdx, setIdx, reps, weight ->
            viewModel.completeSet(exIdx, setIdx, reps, weight)
          },
          onSkipRest = { viewModel.skipRestTimer() },
          onFinish = { viewModel.finishWorkout() },
          onCancel = { viewModel.cancelWorkout() }
        )
      }
    } else {
      // Weekly Beginner Workout Schedule
      item {
        Text(
          text = "WEEKLY BEGINNER SCHEDULE",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
          letterSpacing = 1.sp
        )
      }

      items(weeklySchedule) { schedule ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
          ScheduleDayCard(
            schedule = schedule,
            onStartWorkout = { viewModel.startWorkout(schedule) }
          )
        }
      }

      // Workout History Section
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "COMPLETED WORKOUT LOGS",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
          letterSpacing = 1.sp
        )
      }

      items(workoutHistory) { log ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CyberSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = NeonGreen,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = log,
              fontSize = 13.sp,
              color = TextPrimary,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

@Composable
fun ScheduleDayCard(
  schedule: DayWorkoutSchedule,
  onStartWorkout: () -> Unit
) {
  val isRecovery = schedule.focusArea.contains("Recovery", ignoreCase = true)
  CyberCard(
    borderColor = if (isRecovery) NeonPurple.copy(alpha = 0.4f) else CyberCardBorder
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = schedule.dayName.uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = if (isRecovery) NeonPurple else NeonCyan
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${schedule.estimatedMinutes} Mins",
            fontSize = 12.sp,
            color = TextTertiary
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = schedule.focusArea,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = schedule.exerciseNames.joinToString(", "),
          fontSize = 12.sp,
          color = TextSecondary,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Button(
        onClick = onStartWorkout,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isRecovery) NeonPurple else NeonGreen,
          contentColor = VoidBlack
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Text(
          text = if (isRecovery) "Rest" else "Start",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }
  }
}

@Composable
fun ActiveWorkoutSessionCard(
  session: ActiveWorkoutSession,
  restSeconds: Int,
  onCompleteSet: (Int, Int, Int, Float) -> Unit,
  onSkipRest: () -> Unit,
  onFinish: () -> Unit,
  onCancel: () -> Unit
) {
  CyberCard(
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    borderColor = NeonGreen
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "ACTIVE TRAINING PROTOCOL",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = NeonGreen,
          letterSpacing = 1.sp
        )
        Text(
          text = session.workoutTitle,
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary
        )
      }

      TextButton(onClick = onCancel) {
        Text("Cancel", color = NeonCrimson, fontSize = 12.sp)
      }
    }

    // Animated Rest Timer HUD Bar if rest is active
    AnimatedVisibility(visible = restSeconds > 0) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF132228),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.2f))
                .border(1.5.dp, NeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${restSeconds}s",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = NeonCyan
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "REST INTERVAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
              Text(text = "Catch breath & hydrate", fontSize = 11.sp, color = TextSecondary)
            }
          }

          TextButton(onClick = onSkipRest) {
            Text("Skip Rest", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Exercises and Sets
    session.exercises.forEachIndexed { exIdx, activeEx ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = CyberSurfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "${exIdx + 1}. ${activeEx.exercise.name}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "Target: ${activeEx.exercise.targetMuscles} • ${activeEx.exercise.defaultSetsReps}",
            fontSize = 11.sp,
            color = TextTertiary
          )

          Spacer(modifier = Modifier.height(8.dp))

          activeEx.sets.forEachIndexed { setIdx, set ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Set ${set.setNumber} (${set.targetReps})",
                fontSize = 13.sp,
                color = if (set.isDone) NeonGreen else TextSecondary,
                fontWeight = if (set.isDone) FontWeight.Bold else FontWeight.Normal
              )

              if (set.isDone) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = NeonGreen.copy(alpha = 0.15f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Completed", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                  }
                }
              } else {
                Button(
                  onClick = { onCompleteSet(exIdx, setIdx, 12, 0f) },
                  colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                  Text("Complete Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Button(
      onClick = onFinish,
      colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = VoidBlack),
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    ) {
      Text(
        text = "FINISH WORKOUT (+100 XP)",
        fontWeight = FontWeight.Black,
        fontSize = 14.sp
      )
    }
  }
}
