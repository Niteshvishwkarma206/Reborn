package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.model.UserProfile
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun SettingsScreen(
  viewModel: RebornViewModel,
  user: UserProfile,
  badges: List<AchievementBadge> = emptyList()
) {
  val context = LocalContext.current
  var showResetDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showTargetsDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CleanBackground)
      .testTag("profile_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      RebornTopHeader(
        title = "Profile & Settings",
        subtitle = "Account targets, data export & management"
      )
    }

    // User Profile Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(CleanPrimaryLight)
              .border(1.5.dp, CleanPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = CleanPrimary,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = user.name.ifBlank { "REBORN Athlete" },
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Started: ${user.startDate} • Streak: ${user.streakDays} Days",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }
        }
      }
    }

    // Daily Targets Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp)),
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
            Text(
              text = "DAILY TARGETS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = CleanPrimary,
              letterSpacing = 0.8.sp
            )
            TextButton(
              onClick = { showTargetsDialog = true },
              modifier = Modifier.testTag("btn_edit_targets")
            ) {
              Text("Edit", color = CleanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          TargetSummaryRow(icon = Icons.Default.DirectionsWalk, label = "Daily Step Goal", value = "${user.dailyStepTarget} steps")
          Spacer(modifier = Modifier.height(8.dp))
          TargetSummaryRow(icon = Icons.Default.WaterDrop, label = "Daily Water Goal", value = "${user.dailyWaterTargetMl} ml")
          Spacer(modifier = Modifier.height(8.dp))
          TargetSummaryRow(icon = Icons.Default.Straighten, label = "Distance Target", value = "${user.dailyKmTarget} km")
        }
      }
    }

    // Data Management Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "DATA MANAGEMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.6.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedButton(
            onClick = { showExportDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("btn_export_data"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CleanBorder)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = CleanPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Transformation Data (JSON)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("btn_reset_challenge"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            border = BorderStroke(1.dp, Color(0xFFFECACA))
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset 30-Day Plan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { viewModel.signOut() },
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("btn_sign_out"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = TextPrimary),
            border = BorderStroke(1.dp, CleanBorder)
          ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
          }
        }
      }
    }
  }

  // Edit Targets Dialog
  if (showTargetsDialog) {
    var waterInput by remember { mutableStateOf(user.dailyWaterTargetMl.toString()) }
    var stepInput by remember { mutableStateOf(user.dailyStepTarget.toString()) }
    var kmInput by remember { mutableStateOf(user.dailyKmTarget.toString()) }

    AlertDialog(
      onDismissRequest = { showTargetsDialog = false },
      title = { Text("Edit Daily Goals", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = waterInput,
            onValueChange = { waterInput = it.filter { c -> c.isDigit() } },
            label = { Text("Water Goal (ml)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = stepInput,
            onValueChange = { stepInput = it.filter { c -> c.isDigit() } },
            label = { Text("Daily Step Goal") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = kmInput,
            onValueChange = { kmInput = it },
            label = { Text("Daily Distance (km)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val w = waterInput.toIntOrNull() ?: user.dailyWaterTargetMl
            val s = stepInput.toIntOrNull() ?: user.dailyStepTarget
            val k = kmInput.toFloatOrNull() ?: user.dailyKmTarget
            viewModel.updateTargets(w, s, k)
            showTargetsDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
        ) {
          Text("Save Targets")
        }
      },
      dismissButton = {
        TextButton(onClick = { showTargetsDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Reset Confirmation Dialog
  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Reset 30-Day Plan?", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "This will reset your 30-Day transformation plan back to Day 1. Are you sure?",
          color = TextSecondary,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetChallenge()
            showResetDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
        ) {
          Text("Confirm Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Export Dialog
  if (showExportDialog) {
    val exportData = """
      {
        "app": "reborn",
        "user": "${user.name}",
        "streakDays": ${user.streakDays},
        "challengeDay": ${user.currentChallengeDay},
        "stepGoal": ${user.dailyStepTarget},
        "waterGoal": ${user.dailyWaterTargetMl},
        "timestamp": ${System.currentTimeMillis()}
      }
    """.trimIndent()

    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Progress Data", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Export JSON format:", fontSize = 12.sp, color = TextSecondary)
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = exportData,
              fontSize = 11.sp,
              color = TextPrimary,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Reborn Data", exportData)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            showExportDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
        ) {
          Text("Copy JSON")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) {
          Text("Close", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun TargetSummaryRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = CleanPrimary, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = label, fontSize = 13.sp, color = TextSecondary)
    }
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
  }
}
