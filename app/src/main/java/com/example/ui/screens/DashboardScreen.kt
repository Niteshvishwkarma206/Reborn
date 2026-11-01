package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyMission
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
  val todayMissions = missions.filter { it.day == user.currentChallengeDay }
  val completedMissions = missions.count { it.isCompleted }
  val totalMissions = missions.size
  val overallPercentage = if (totalMissions > 0) (completedMissions * 100) / totalMissions else 0

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "REBORN",
        subtitle = "REBUILD YOURSELF. LEVEL UP YOUR LIFE.",
        streakDays = user.streakDays
      )
    }

    // Hero Transformation Protocol Status Banner
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = NeonGreen.copy(alpha = 0.5f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "CHALLENGE PROGRESS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonGreen,
              letterSpacing = 1.sp
            )
            Text(
              text = "Day ${user.currentChallengeDay} of 30",
              fontSize = 24.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
          }

          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  listOf(NeonGreen.copy(alpha = 0.25f), Color.Transparent)
                )
              )
              .border(2.dp, NeonGreen, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$overallPercentage%",
              fontWeight = FontWeight.Black,
              fontSize = 14.sp,
              color = NeonGreen
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "${completedMissions} of ${totalMissions} transformation missions conquered",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    // Player XP and Level Bar
    item {
      XpProgressBar(
        level = user.level,
        currentXp = user.currentXp,
        xpToNextLevel = user.xpToNextLevel,
        rankTitle = user.title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }

    // Quick Command Access Strip (Horizontal Scroll for All 10 Domains)
    item {
      Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
          text = "COMMAND MODULES",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          letterSpacing = 1.sp
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val modules = listOf(
            Triple("30-Day", Icons.Default.Flag, AppScreen.CHALLENGE),
            Triple("Workout", Icons.Default.FitnessCenter, AppScreen.WORKOUT),
            Triple("Exercises", Icons.Default.FormatListBulleted, AppScreen.EXERCISES),
            Triple("Stride", Icons.Default.DirectionsWalk, AppScreen.WALKING),
            Triple("Fuel", Icons.Default.Restaurant, AppScreen.NUTRITION),
            Triple("Mind", Icons.Default.Psychology, AppScreen.MIND),
            Triple("Persona", Icons.Default.MilitaryTech, AppScreen.PERSONALITY),
            Triple("Analytics", Icons.Default.BarChart, AppScreen.PROGRESS)
          )

          items(modules) { (label, icon, targetScreen) ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = CyberSurface,
              border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
              modifier = Modifier.clickable { onNavigate(targetScreen) }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = label,
                  tint = NeonGreen,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = label,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
            }
          }
        }
      }
    }

    // Core Metrics Grid (2x2 HUD Tiles)
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricHudTile(
            title = "Workouts",
            value = "2 Done",
            subtitle = "Active Protocol",
            icon = Icons.Default.FitnessCenter,
            accentColor = NeonGreen,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(AppScreen.WORKOUT) }
          )
          MetricHudTile(
            title = "Stride Dist",
            value = "7.2 km",
            subtitle = "Goal: 5.0 km/day",
            icon = Icons.Default.DirectionsWalk,
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(AppScreen.WALKING) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricHudTile(
            title = "Hydration",
            value = "2.25 L",
            subtitle = "+250ml Quick Add",
            icon = Icons.Default.WaterDrop,
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.addWater(250) }
          )
          MetricHudTile(
            title = "Focus Rating",
            value = "8 / 10",
            subtitle = "Mind Fortress",
            icon = Icons.Default.Psychology,
            accentColor = NeonPurple,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(AppScreen.MIND) }
          )
        }
      }
    }

    // Today's Missions Section
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "TODAY'S MISSIONS (DAY ${user.currentChallengeDay})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
          )
          Text(
            text = "VIEW ALL 30 DAYS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            modifier = Modifier.clickable { onNavigate(AppScreen.CHALLENGE) }
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (todayMissions.isEmpty()) {
          Text(
            text = "No missions loaded for today.",
            color = TextTertiary,
            fontSize = 13.sp
          )
        } else {
          todayMissions.forEach { mission ->
            MissionRowItem(
              mission = mission,
              onToggle = { viewModel.toggleMission(mission) }
            )
            Spacer(modifier = Modifier.height(8.dp))
          }
        }
      }
    }

    // Weekly Activity Visualizer (Mon-Sun Bar Chart)
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        borderColor = CyberCardBorder
      ) {
        Text(
          text = "WEEKLY CONSISTENCY RADAR",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        val days = listOf("M" to 0.8f, "T" to 0.9f, "W" to 1.0f, "T" to 0.7f, "F" to 0.85f, "S" to 0.6f, "S" to 0.4f)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          days.forEach { (d, ratio) ->
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom
            ) {
              Box(
                modifier = Modifier
                  .width(18.dp)
                  .height((70 * ratio).dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(
                    Brush.verticalGradient(
                      listOf(NeonCyan, NeonGreen)
                    )
                  )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = d,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (ratio >= 0.8f) NeonGreen else TextTertiary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MissionRowItem(
  mission: DailyMission,
  onToggle: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (mission.isCompleted) Color(0xFF0F1B1C) else CyberSurface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (mission.isCompleted) NeonGreen.copy(alpha = 0.5f) else CyberCardBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(26.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(
            if (mission.isCompleted) NeonGreen else Color.Transparent
          )
          .border(
            1.5.dp,
            if (mission.isCompleted) NeonGreen else TextTertiary,
            RoundedCornerShape(6.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        if (mission.isCompleted) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Completed",
            tint = VoidBlack,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = mission.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = if (mission.isCompleted) TextSecondary else TextPrimary
        )
        Text(
          text = mission.description,
          fontSize = 11.sp,
          color = TextTertiary,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(mission.category.badgeColor).copy(alpha = 0.15f)
      ) {
        Text(
          text = "+${mission.xpReward} XP",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(mission.category.badgeColor),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}
