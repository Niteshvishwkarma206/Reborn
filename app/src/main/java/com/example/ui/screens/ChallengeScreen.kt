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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyMission
import com.example.data.model.MissionCategory
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChallengeScreen(
  viewModel: RebornViewModel,
  missions: List<DailyMission>,
  selectedDay: Int
) {
  var activeCategoryFilter by remember { mutableStateOf<MissionCategory?>(null) }

  val dayMissions = missions.filter { it.day == selectedDay }
  val filteredMissions = if (activeCategoryFilter == null) {
    dayMissions
  } else {
    dayMissions.filter { it.category == activeCategoryFilter }
  }

  val completedDayCount = dayMissions.count { it.isCompleted }
  val totalDayCount = dayMissions.size
  val isBossMilestone = selectedDay in listOf(7, 14, 21, 30)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "30-DAY PROTOCOL",
        subtitle = "SELECT A DAY TO VIEW AND EXECUTE MISSIONS"
      )
    }

    // Day 1 to 30 Horizontal Rail
    item {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items((1..30).toList()) { day ->
          val isSelected = day == selectedDay
          val isCompletedDay = missions.filter { it.day == day }.all { it.isCompleted } && missions.any { it.day == day }
          val isMilestone = day in listOf(7, 14, 21, 30)

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
              isSelected -> NeonCyan.copy(alpha = 0.2f)
              isCompletedDay -> NeonGreen.copy(alpha = 0.15f)
              else -> CyberSurface
            },
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              when {
                isSelected -> NeonCyan
                isCompletedDay -> NeonGreen
                isMilestone -> NeonAmber
                else -> CyberCardBorder
              }
            ),
            modifier = Modifier
              .clickable { viewModel.selectChallengeDay(day) }
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              if (isMilestone) {
                Text(text = "👑", fontSize = 10.sp)
              }
              Text(
                text = "DAY $day",
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = when {
                  isSelected -> NeonCyan
                  isCompletedDay -> NeonGreen
                  else -> TextSecondary
                }
              )
              if (isCompletedDay) {
                Text(
                  text = "DONE",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonGreen
                )
              }
            }
          }
        }
      }
    }

    // Day Header Card
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        borderColor = if (isBossMilestone) NeonAmber else CyberCardBorder
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "DAY $selectedDay OBJECTIVES",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
              if (isBossMilestone) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = NeonAmber.copy(alpha = 0.2f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber)
                ) {
                  Text(
                    text = "BOSS MILESTONE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonAmber,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(
              text = "$completedDayCount / $totalDayCount missions completed",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }

          val dayRatio = if (totalDayCount > 0) completedDayCount.toFloat() / totalDayCount else 0f
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(CyberSurfaceVariant)
              .border(2.dp, if (dayRatio >= 1f) NeonGreen else NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${(dayRatio * 100).toInt()}%",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = if (dayRatio >= 1f) NeonGreen else NeonCyan
            )
          }
        }
      }
    }

    // Category Filter Chips
    item {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        item {
          FilterChip(
            selected = activeCategoryFilter == null,
            onClick = { activeCategoryFilter = null },
            label = { Text("All Categories") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
              selectedLabelColor = NeonCyan,
              containerColor = CyberSurface,
              labelColor = TextSecondary
            )
          )
        }
        items(MissionCategory.values()) { category ->
          FilterChip(
            selected = activeCategoryFilter == category,
            onClick = {
              activeCategoryFilter = if (activeCategoryFilter == category) null else category
            },
            label = { Text(category.label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(category.badgeColor).copy(alpha = 0.2f),
              selectedLabelColor = Color(category.badgeColor),
              containerColor = CyberSurface,
              labelColor = TextSecondary
            )
          )
        }
      }
    }

    // Missions List
    item {
      Spacer(modifier = Modifier.height(6.dp))
    }

    items(filteredMissions, key = { it.id }) { mission ->
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        DetailedMissionCard(
          mission = mission,
          onToggle = { viewModel.toggleMission(mission) }
        )
      }
    }
  }
}

@Composable
fun DetailedMissionCard(
  mission: DailyMission,
  onToggle: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (mission.isCompleted) Color(0xFF0F1E1B) else CyberSurface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (mission.isCompleted) NeonGreen.copy(alpha = 0.45f) else CyberCardBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(mission.category.badgeColor).copy(alpha = 0.15f)
        ) {
          Text(
            text = mission.category.label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(mission.category.badgeColor),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = NeonCyan.copy(alpha = 0.15f)
        ) {
          Text(
            text = "+${mission.xpReward} XP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (mission.isCompleted) NeonGreen else Color.Transparent)
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
              contentDescription = "Done",
              tint = VoidBlack,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = mission.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (mission.isCompleted) TextSecondary else TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = mission.description,
            fontSize = 12.sp,
            color = TextTertiary,
            lineHeight = 17.sp
          )

          if (mission.isCompleted && mission.completedAt != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Completed ${dateFormat.format(Date(mission.completedAt))}",
                fontSize = 10.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }
  }
}
