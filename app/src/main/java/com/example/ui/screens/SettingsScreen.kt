package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.model.UserProfile
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun SettingsScreen(
  viewModel: RebornViewModel,
  user: UserProfile,
  badges: List<AchievementBadge>
) {
  val context = LocalContext.current
  var showResetDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var exportFormat by remember { mutableStateOf("JSON") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "COMMAND & SETTINGS",
        subtitle = "IDENTITY, TARGETS & ACHIEVEMENTS"
      )
    }

    // Hunter Profile Card
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = NeonCyan.copy(alpha = 0.5f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.2f))
              .border(2.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "⚡", fontSize = 24.sp)
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = user.name,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
            Text(
              text = "${user.title} • ${user.rankTier}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NeonGreen
            )
            Text(
              text = user.startDate,
              fontSize = 11.sp,
              color = TextTertiary
            )
          }
        }
      }
    }

    // Achievements & Badges Gallery
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = NeonAmber.copy(alpha = 0.4f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "TROPHY VAULT & BADGES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonAmber,
            letterSpacing = 1.sp
          )
          Text(
            text = "${badges.count { it.isUnlocked }} / ${badges.size} UNLOCKED",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        badges.forEach { badge ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (badge.isUnlocked) CyberSurfaceVariant else Color(0xFF10131A),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (badge.isUnlocked) NeonAmber.copy(alpha = 0.5f) else CyberCardBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = badge.iconEmoji,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 10.dp)
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = badge.title,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (badge.isUnlocked) TextPrimary else TextTertiary
                )
                Text(
                  text = badge.description,
                  fontSize = 11.sp,
                  color = TextTertiary
                )
              }
              if (badge.isUnlocked) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = NeonAmber.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "+${badge.xpBonus} XP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonAmber,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // System Actions (Export & Reset)
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CyberCardBorder
      ) {
        Text(
          text = "DATA MANAGEMENT & BACKUP",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = {
            exportFormat = "JSON"
            showExportDialog = true
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("EXPORT PROGRESS AS JSON / CSV", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = { showResetDialog = true },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson.copy(alpha = 0.2f), contentColor = NeonCrimson),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCrimson)
        ) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = NeonCrimson, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("RESET 30-DAY CHALLENGE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = { viewModel.signOut() },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant, contentColor = TextPrimary),
          border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
          Icon(Icons.Default.ExitToApp, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("LOGOUT / SWITCH HUNTER ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
        }
      }
    }
  }

  // Reset Confirmation Dialog
  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Reset Challenge?", color = NeonCrimson, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "This will reset your 30-Day mission progress, active XP, and stride logs back to Day 1. Are you sure you wish to restart your rebirth?",
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
          colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson, contentColor = Color.White)
        ) {
          Text("Confirm Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = CyberSurface
    )
  }

  // Export Dialog
  if (showExportDialog) {
    val exportData = """
      {
        "app": "reborn",
        "user": "${user.name}",
        "level": ${user.level},
        "xp": ${user.currentXp},
        "streakDays": ${user.streakDays},
        "challengeDay": ${user.currentChallengeDay},
        "rank": "${user.rankTier}",
        "timestamp": ${System.currentTimeMillis()}
      }
    """.trimIndent()

    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Progress Data", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Your progress dossier is generated below:", color = TextSecondary, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = VoidBlack,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = exportData,
              color = NeonGreen,
              fontSize = 11.sp,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Reborn Data", exportData))
            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            showExportDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
        ) {
          Text("Copy to Clipboard")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) {
          Text("Close", color = TextSecondary)
        }
      },
      containerColor = CyberSurface
    )
  }
}
