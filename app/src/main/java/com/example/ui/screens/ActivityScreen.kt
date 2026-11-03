package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DailyStepCount
import com.example.data.model.StepSource
import com.example.data.model.UserProfile
import com.example.data.model.WalkingActivity
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel
import java.util.UUID

@Composable
fun ActivityScreen(
  viewModel: RebornViewModel,
  user: UserProfile,
  activities: List<WalkingActivity>
) {
  val stepState by viewModel.stepTrackerState.collectAsStateWithLifecycle()
  var showManualStepDialog by remember { mutableStateOf(false) }
  var showGoalDialog by remember { mutableStateOf(false) }
  var showLogSessionDialog by remember { mutableStateOf(false) }

  // Health Connect permissions launcher
  val requestPermissionsContract = remember {
    PermissionController.createRequestPermissionResultContract()
  }
  val requestPermissionsLauncher = rememberLauncherForActivityResult(
    contract = requestPermissionsContract
  ) { grantedPermissions ->
    val stepsGranted = grantedPermissions.contains(HealthPermission.getReadPermission(StepsRecord::class))
    if (stepsGranted) {
      viewModel.refreshStepTracker()
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CleanBackground)
      .testTag("activity_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      RebornTopHeader(
        title = "Activity & Steps",
        subtitle = "Real-time step tracker & walk activity logs"
      )
    }

    // Step Tracker Hero Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_step_hero"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Top row: Title and Source Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TODAY'S STEP COUNT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CleanPrimary,
                letterSpacing = 0.8.sp
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${stepState.todaySteps}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
              )
            }

            // Source Indicator Badge
            val (badgeBg, badgeBorder, badgeText, badgeColor) = when (stepState.source) {
              StepSource.HEALTH_CONNECT -> Quadruple(
                CleanPrimaryLight,
                CleanPrimary,
                "Health Connect",
                CleanPrimaryDark
              )
              StepSource.MANUAL -> Quadruple(
                Color(0xFFEFF6FF),
                Color(0xFF93C5FD),
                "Manual Entry",
                Color(0xFF1D4ED8)
              )
              StepSource.NONE -> Quadruple(
                Color(0xFFF1F5F9),
                Color(0xFFCBD5E1),
                "No Data Logged",
                TextSecondary
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = badgeBg,
              border = BorderStroke(1.dp, badgeBorder)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = badgeText,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = badgeColor
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Progress bar towards step goal
          val progressFraction = if (stepState.dailyGoal > 0) {
            (stepState.todaySteps.toFloat() / stepState.dailyGoal).coerceIn(0f, 1f)
          } else 0f

          LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = CleanPrimary,
            trackColor = Color(0xFFE2E8F0)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Sub-metrics row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Goal: ${stepState.dailyGoal} steps",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
            Text(
              text = "Est. Distance: ${String.format("%.2f", stepState.distanceKm)} km",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Action Buttons: Connect Health Connect or Manual Entry or Edit Goal
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Health Connect Sync / Permission Button
            if (stepState.isHealthConnectAvailable) {
              Button(
                onClick = {
                  val permissions = setOf(HealthPermission.getReadPermission(StepsRecord::class))
                  requestPermissionsLauncher.launch(permissions)
                },
                modifier = Modifier
                  .weight(1f)
                  .height(40.dp)
                  .testTag("btn_sync_health_connect"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
              ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (stepState.isPermissionGranted) "Sync Health" else "Connect Health",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Manual Entry Button
            OutlinedButton(
              onClick = { showManualStepDialog = true },
              modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("btn_manual_steps"),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, CleanBorder)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = CleanPrimary)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Log Steps", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            // Edit Goal Button
            IconButton(
              onClick = { showGoalDialog = true },
              modifier = Modifier
                .size(40.dp)
                .border(1.dp, CleanBorder, RoundedCornerShape(10.dp))
                .testTag("btn_edit_step_goal")
            ) {
              Icon(Icons.Default.Flag, contentDescription = "Edit Goal", tint = CleanPrimary, modifier = Modifier.size(20.dp))
            }
          }
        }
      }
    }

    // Weekly Step History Bar Chart
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_weekly_step_history"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "WEEKLY STEP HISTORY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.6.sp
          )
          Spacer(modifier = Modifier.height(14.dp))

          // 7-Day Bar Chart
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            stepState.weeklyHistory.forEach { dayCount ->
              WeeklyStepBar(dayCount = dayCount, maxSteps = (stepState.dailyGoal * 1.25f).toInt())
            }
          }
        }
      }
    }

    // Walk / Run Activities Header & Log CTA
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "WALK & RUN SESSIONS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.6.sp
        )
        TextButton(
          onClick = { showLogSessionDialog = true },
          modifier = Modifier.testTag("btn_log_session")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = CleanPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Log Session", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CleanPrimary)
        }
      }
    }

    if (activities.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CleanBorder, RoundedCornerShape(12.dp)),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = CleanCard)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No sessions logged yet. Tap 'Log Session' to record your walk or run.",
              color = TextSecondary,
              fontSize = 13.sp
            )
          }
        }
      }
    } else {
      items(activities, key = { it.id }) { act ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CleanBorder, RoundedCornerShape(12.dp)),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = CleanCard)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CleanEmeraldLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (act.activityType.contains("Run", ignoreCase = true)) Icons.Default.DirectionsRun else Icons.Default.DirectionsWalk,
                contentDescription = null,
                tint = CleanPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${act.activityType} • ${act.distanceKm} km",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "${act.durationMinutes} mins • ${act.steps} steps • ${act.date}",
                fontSize = 12.sp,
                color = TextSecondary
              )
              if (act.notes.isNotBlank()) {
                Text(
                  text = act.notes,
                  fontSize = 11.sp,
                  color = CleanPrimaryDark
                )
              }
            }
          }
        }
      }
    }
  }

  // Manual Steps Dialog
  if (showManualStepDialog) {
    var stepInput by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showManualStepDialog = false },
      title = { Text("Log Steps Manually", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
      text = {
        Column {
          Text("Enter today's step count manually:", fontSize = 13.sp, color = TextSecondary)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = stepInput,
            onValueChange = { stepInput = it.filter { ch -> ch.isDigit() } },
            placeholder = { Text("e.g. 7500") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val count = stepInput.toIntOrNull()
            if (count != null && count >= 0) {
              viewModel.recordManualSteps(count)
              showManualStepDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showManualStepDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Edit Step Goal Dialog
  if (showGoalDialog) {
    var goalInput by remember { mutableStateOf(stepState.dailyGoal.toString()) }
    AlertDialog(
      onDismissRequest = { showGoalDialog = false },
      title = { Text("Change Daily Step Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
      text = {
        Column {
          Text("Set your daily target steps:", fontSize = 13.sp, color = TextSecondary)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = goalInput,
            onValueChange = { goalInput = it.filter { ch -> ch.isDigit() } },
            placeholder = { Text("e.g. 10000") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val g = goalInput.toIntOrNull()
            if (g != null && g > 0) {
              viewModel.updateStepGoal(g)
              showGoalDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
        ) {
          Text("Save Goal")
        }
      },
      dismissButton = {
        TextButton(onClick = { showGoalDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Log Walk/Run Session Dialog
  if (showLogSessionDialog) {
    var distStr by remember { mutableStateOf("") }
    var durStr by remember { mutableStateOf("") }
    var stepsStr by remember { mutableStateOf("") }
    var isRun by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showLogSessionDialog = false },
      title = { Text("Log Walk / Run Session", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = !isRun,
              onClick = { isRun = false },
              label = { Text("Walking") }
            )
            FilterChip(
              selected = isRun,
              onClick = { isRun = true },
              label = { Text("Running") }
            )
          }
          OutlinedTextField(
            value = distStr,
            onValueChange = { distStr = it },
            label = { Text("Distance (km)") },
            placeholder = { Text("e.g. 3.5") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = durStr,
            onValueChange = { durStr = it.filter { c -> c.isDigit() } },
            label = { Text("Duration (minutes)") },
            placeholder = { Text("e.g. 30") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = stepsStr,
            onValueChange = { stepsStr = it.filter { c -> c.isDigit() } },
            label = { Text("Estimated Steps") },
            placeholder = { Text("e.g. 4500") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val d = distStr.toFloatOrNull() ?: 0f
            val dur = durStr.toIntOrNull() ?: 0
            val s = stepsStr.toIntOrNull() ?: ((d * 1300).toInt())
            if (d > 0f) {
              viewModel.recordWalking(
                WalkingActivity(
                  id = UUID.randomUUID().toString(),
                  date = "Today",
                  activityType = if (isRun) "Running" else "Walking",
                  distanceKm = d,
                  durationMinutes = dur,
                  steps = s
                )
              )
              showLogSessionDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
        ) {
          Text("Save Session")
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogSessionDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun WeeklyStepBar(
  dayCount: DailyStepCount,
  maxSteps: Int
) {
  val safeMax = if (maxSteps > 0) maxSteps else 10000
  val heightFraction = (dayCount.steps.toFloat() / safeMax).coerceIn(0.08f, 1f)
  val isGoalMet = dayCount.steps >= dayCount.goal && dayCount.goal > 0

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Bottom,
    modifier = Modifier.width(36.dp)
  ) {
    if (dayCount.steps > 0) {
      Text(
        text = if (dayCount.steps >= 1000) "${dayCount.steps / 1000}k" else "${dayCount.steps}",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = if (isGoalMet) CleanPrimaryDark else TextSecondary
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    Box(
      modifier = Modifier
        .width(18.dp)
        .fillMaxHeight(heightFraction * 0.72f)
        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
        .background(
          if (isGoalMet) CleanPrimary else if (dayCount.steps > 0) Color(0xFF93C5FD) else Color(0xFFE2E8F0)
        )
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = dayCount.dayOfWeek,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = TextSecondary
    )
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
