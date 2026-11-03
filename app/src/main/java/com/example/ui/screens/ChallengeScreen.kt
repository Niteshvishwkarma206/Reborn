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
        title = "30-Day Transformation",
        subtitle = "Daily habit schedule and progress tracking"
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
              isSelected -> CleanPrimary
              isCompletedDay -> CleanPrimaryLight
              else -> CleanCard
            },
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              when {
                isSelected -> CleanPrimaryDark
                isCompletedDay -> CleanPrimary
                isMilestone -> CleanAmber
                else -> CleanBorder
              }
            ),
            shadowElevation = if (isSelected) 2.dp else 1.dp,
            modifier = Modifier
              .clickable { viewModel.selectChallengeDay(day) }
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              if (isMilestone) {
                Text(text = "⭐", fontSize = 10.sp)
              }
              Text(
                text = "Day $day",
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                  isSelected -> Color.White
                  isCompletedDay -> CleanPrimaryDark
                  else -> TextPrimary
                }
              )
              if (isCompletedDay) {
                Text(
                  text = "Done",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else CleanPrimaryDark
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
        borderColor = CleanBorder
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Day $selectedDay Objectives",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              if (isBossMilestone) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = CleanAmberLight,
                  border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                  Text(
                    text = "MILESTONE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanAmber,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(
              text = "$completedDayCount of $totalDayCount missions completed",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }

          val dayRatio = if (totalDayCount > 0) completedDayCount.toFloat() / totalDayCount else 0f
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(if (dayRatio >= 1f) CleanPrimaryLight else CleanCardVariant)
              .border(2.dp, if (dayRatio >= 1f) CleanPrimary else CleanBorderHover, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${(dayRatio * 100).toInt()}%",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (dayRatio >= 1f) CleanPrimaryDark else TextPrimary
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
              selectedContainerColor = CleanPrimaryLight,
              selectedLabelColor = CleanPrimaryDark,
              containerColor = CleanCard,
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
              selectedContainerColor = CleanPrimaryLight,
              selectedLabelColor = CleanPrimaryDark,
              containerColor = CleanCard,
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
    color = if (mission.isCompleted) CleanPrimaryLight else CleanCard,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (mission.isCompleted) CleanPrimary.copy(alpha = 0.4f) else CleanBorder
    ),
    shadowElevation = if (mission.isCompleted) 0.dp else 1.dp,
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
          color = CleanCardVariant
        ) {
          Text(
            text = mission.category.label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CleanPrimaryLight
        ) {
          Text(
            text = "+${mission.xpReward} pts",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CleanPrimaryDark,
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
            .background(if (mission.isCompleted) CleanPrimary else Color.Transparent)
            .border(
              1.5.dp,
              if (mission.isCompleted) CleanPrimary else CleanBorderHover,
              RoundedCornerShape(6.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          if (mission.isCompleted) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Done",
              tint = Color.White,
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
