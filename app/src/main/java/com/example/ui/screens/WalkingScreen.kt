package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.UserProfile
import com.example.data.model.WalkingActivity
import com.example.ui.components.CyberCard
import com.example.ui.components.MetricHudTile
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel
import java.util.UUID

@Composable
fun WalkingScreen(
  viewModel: RebornViewModel,
  user: UserProfile,
  activities: List<WalkingActivity>
) {
  var showLogDialog by remember { mutableStateOf(false) }
  var showTargetDialog by remember { mutableStateOf(false) }

  val totalDistanceKm = activities.sumOf { it.distanceKm.toDouble() }.toFloat()
  val todayDistanceKm = activities.filter { it.date.equals("Today", ignoreCase = true) }
    .sumOf { it.distanceKm.toDouble() }.toFloat()
  val weeklyDistanceKm = totalDistanceKm.coerceAtMost(35.0f)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "STRIDE TELEMETRY",
        subtitle = "WALKING & RUNNING DISTANCE TRACKER"
      )
    }

    // Three Primary Distance HUD Tiles
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricHudTile(
            title = "Today Dist",
            value = "%.1f km".format(todayDistanceKm),
            subtitle = "Target: %.1f km".format(user.dailyKmTarget),
            icon = Icons.Default.DirectionsWalk,
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f)
          )
          MetricHudTile(
            title = "Total Dist",
            value = "%.1f km".format(totalDistanceKm),
            subtitle = "All-time Challenge",
            icon = Icons.Default.DirectionsRun,
            accentColor = NeonGreen,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Target Management Card
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CyberCardBorder
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "PERSONAL TARGETS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              letterSpacing = 1.sp
            )
            Text(
              text = "Daily: %.1f km • %d Steps".format(user.dailyKmTarget, user.dailyStepTarget),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }
          OutlinedButton(
            onClick = { showTargetDialog = true },
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text("Edit Target", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Record New Stride CTA Button
    item {
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Button(
          onClick = { showLogDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(vertical = 12.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("LOG WALK OR RUN SESSION", fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
      }
    }

    // Activity Log History Header
    item {
      Text(
        text = "STRIDE ACTIVITY LOGS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        letterSpacing = 1.sp
      )
    }

    if (activities.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No walks or runs recorded yet. Take the first step!", color = TextTertiary, fontSize = 13.sp)
        }
      }
    } else {
      items(activities, key = { it.id }) { act ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
          StrideActivityCard(activity = act)
        }
      }
    }
  }

  if (showLogDialog) {
    LogStrideDialog(
      onDismiss = { showLogDialog = false },
      onSave = { type, km, minutes, steps, notes ->
        viewModel.recordWalking(
          WalkingActivity(
            id = UUID.randomUUID().toString(),
            date = "Today",
            activityType = type,
            distanceKm = km,
            durationMinutes = minutes,
            steps = steps,
            notes = notes
          )
        )
        showLogDialog = false
      }
    )
  }

  if (showTargetDialog) {
    EditTargetDialog(
      currentTargetKm = user.dailyKmTarget,
      currentTargetSteps = user.dailyStepTarget,
      onDismiss = { showTargetDialog = false },
      onSave = { km, steps ->
        viewModel.updateTargets(user.dailyWaterTargetMl, steps, km)
        showTargetDialog = false
      }
    )
  }
}

@Composable
fun StrideActivityCard(activity: WalkingActivity) {
  val isRun = activity.activityType.equals("Running", ignoreCase = true)
  CyberCard(
    borderColor = if (isRun) NeonAmber.copy(alpha = 0.4f) else NeonCyan.copy(alpha = 0.3f)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = (if (isRun) NeonAmber else NeonCyan).copy(alpha = 0.15f)
        ) {
          Icon(
            imageVector = if (isRun) Icons.Default.DirectionsRun else Icons.Default.DirectionsWalk,
            contentDescription = null,
            tint = if (isRun) NeonAmber else NeonCyan,
            modifier = Modifier
              .padding(8.dp)
              .size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "${activity.activityType} • ${activity.date}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "${activity.durationMinutes} mins • ${activity.steps} steps",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }

      Text(
        text = "%.2f km".format(activity.distanceKm),
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = if (isRun) NeonAmber else NeonCyan
      )
    }

    if (activity.notes.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Note: ${activity.notes}",
        fontSize = 12.sp,
        color = TextTertiary
      )
    }
  }
}

@Composable
fun LogStrideDialog(
  onDismiss: () -> Unit,
  onSave: (String, Float, Int, Int, String) -> Unit
) {
  var activityType by remember { mutableStateOf("Walking") }
  var distanceKm by remember { mutableStateOf("4.5") }
  var durationMin by remember { mutableStateOf("45") }
  var steps by remember { mutableStateOf("5600") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Log Walk / Run Session", fontWeight = FontWeight.Bold, color = TextPrimary) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = activityType == "Walking",
            onClick = { activityType = "Walking" },
            label = { Text("Walking") }
          )
          FilterChip(
            selected = activityType == "Running",
            onClick = { activityType = "Running" },
            label = { Text("Running") }
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = distanceKm,
          onValueChange = { distanceKm = it },
          label = { Text("Distance (km)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = durationMin,
          onValueChange = { durationMin = it },
          label = { Text("Duration (minutes)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = steps,
          onValueChange = { steps = it },
          label = { Text("Estimated Steps") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (optional)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val km = distanceKm.toFloatOrNull() ?: 3.0f
          val mins = durationMin.toIntOrNull() ?: 30
          val st = steps.toIntOrNull() ?: 4000
          onSave(activityType, km, mins, st, notes)
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
      ) {
        Text("Save Stride")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
    },
    containerColor = CyberSurface
  )
}

@Composable
fun EditTargetDialog(
  currentTargetKm: Float,
  currentTargetSteps: Int,
  onDismiss: () -> Unit,
  onSave: (Float, Int) -> Unit
) {
  var km by remember { mutableStateOf(currentTargetKm.toString()) }
  var steps by remember { mutableStateOf(currentTargetSteps.toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Activity Target", fontWeight = FontWeight.Bold, color = TextPrimary) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = km,
          onValueChange = { km = it },
          label = { Text("Daily Distance Target (km)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = steps,
          onValueChange = { steps = it },
          label = { Text("Daily Step Target") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val newKm = km.toFloatOrNull() ?: 5.0f
          val newSteps = steps.toIntOrNull() ?: 8000
          onSave(newKm, newSteps)
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
      ) {
        Text("Save Target")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
    },
    containerColor = CyberSurface
  )
}
