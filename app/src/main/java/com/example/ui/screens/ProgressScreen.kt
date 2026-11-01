package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.MissionCategory
import com.example.data.model.UserProfile
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*

@Composable
fun ProgressScreen(
  user: UserProfile,
  missions: List<DailyMission>
) {
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
        title = "TRANSFORMATION RADAR",
        subtitle = "DAY 1 VS DAY 30 PROGRESS METRICS"
      )
    }

    // Hero Overall Transformation
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = NeonCyan.copy(alpha = 0.5f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "TRANSFORMATION COMPLETION",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              letterSpacing = 1.sp
            )
            Text(
              text = "$overallPercentage%",
              fontSize = 32.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
            Text(
              text = "Active Day: ${user.currentChallengeDay} / 30",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NeonGreen
            )
          }

          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent))
              )
              .border(2.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${user.rankTier}",
              fontWeight = FontWeight.Black,
              fontSize = 13.sp,
              color = NeonCyan
            )
          }
        }
      }
    }

    // Day 1 vs Day 30 Comparison Matrix
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = NeonPurple.copy(alpha = 0.4f)
      ) {
        Text(
          text = "DAY 1 VS DAY 30 METRICS COMPARISON",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = NeonPurple,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        ComparisonRow("Daily Push-ups", "12 reps", "45+ reps", "+275%")
        ComparisonRow("Daily Squats", "15 reps", "60+ reps", "+300%")
        ComparisonRow("Stride Distance", "3.0 km", "8.0 km", "+166%")
        ComparisonRow("Focus Index", "4 / 10", "9 / 10", "+125%")
        ComparisonRow("Eye Contact", "Hesitant", "Unbroken / Alpha", "Mastered")
        ComparisonRow("Daily Water", "1,500 ml", "3,500 ml", "+133%")
      }
    }

    // Category-wise Breakdown
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CyberCardBorder
      ) {
        Text(
          text = "CATEGORY MASTERY RATIOS",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        MissionCategory.values().forEach { category ->
          val catMissions = missions.filter { it.category == category }
          val done = catMissions.count { it.isCompleted }
          val total = catMissions.size
          val ratio = if (total > 0) done.toFloat() / total.toFloat() else 0f

          Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = category.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              Text(
                text = "$done / $total (${(ratio * 100).toInt()}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(category.badgeColor)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { ratio },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = Color(category.badgeColor),
              trackColor = Color(0xFF1E2433)
            )
          }
        }
      }
    }
  }
}

@Composable
fun ComparisonRow(
  metric: String,
  day1Value: String,
  day30Target: String,
  growth: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1.2f)) {
      Text(text = metric, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
      Text(text = "Day 1: $day1Value", fontSize = 11.sp, color = TextTertiary)
    }
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = "Target: $day30Target", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
    }
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = NeonGreen.copy(alpha = 0.15f)
    ) {
      Text(
        text = growth,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = NeonGreen,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}
