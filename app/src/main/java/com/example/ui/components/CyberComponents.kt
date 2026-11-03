package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@Composable
fun CyberCard(
  modifier: Modifier = Modifier,
  borderColor: Color = CleanBorder,
  backgroundColor: Color = CleanCard,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      content = content
    )
  }
}

@Composable
fun CircularWorkoutProgress(
  percentage: Int,
  modifier: Modifier = Modifier,
  size: Dp = 72.dp,
  strokeWidth: Dp = 6.dp,
  label: String? = null
) {
  val cleanProgress = (percentage / 100f).coerceIn(0f, 1f)
  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(
      progress = { 1f },
      modifier = Modifier.fillMaxSize(),
      color = Color(0xFFE2E8F0),
      strokeWidth = strokeWidth,
      strokeCap = StrokeCap.Round
    )
    CircularProgressIndicator(
      progress = { cleanProgress },
      modifier = Modifier.fillMaxSize(),
      color = CleanPrimary,
      strokeWidth = strokeWidth,
      strokeCap = StrokeCap.Round
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "$percentage%",
        fontSize = if (size > 60.dp) 15.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      if (label != null) {
        Text(
          text = label,
          fontSize = 9.sp,
          fontWeight = FontWeight.Medium,
          color = TextSecondary
        )
      }
    }
  }
}

@Composable
fun MetricHudTile(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .border(1.dp, CleanBorder, RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CleanCard),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.5.sp
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 12.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
fun RebornBottomNavigation(
  currentScreen: AppScreen,
  onScreenSelected: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  val primaryScreens = listOf(
    AppScreen.HOME to Icons.Default.Home,
    AppScreen.PLAN to Icons.Default.EventNote,
    AppScreen.ACTIVITY to Icons.Default.DirectionsWalk,
    AppScreen.PROGRESS to Icons.Default.Insights,
    AppScreen.PROFILE to Icons.Default.Person
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars),
    color = CleanCard,
    tonalElevation = 2.dp,
    border = BorderStroke(1.dp, CleanBorder)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp, horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      primaryScreens.forEach { (screen, icon) ->
        val isSelected = when (screen) {
          AppScreen.HOME -> currentScreen == AppScreen.HOME
          AppScreen.PLAN -> currentScreen in listOf(AppScreen.PLAN, AppScreen.CHALLENGE, AppScreen.WORKOUT, AppScreen.EXERCISES)
          AppScreen.ACTIVITY -> currentScreen in listOf(AppScreen.ACTIVITY, AppScreen.WALKING)
          AppScreen.PROGRESS -> currentScreen == AppScreen.PROGRESS
          AppScreen.PROFILE -> currentScreen in listOf(AppScreen.PROFILE, AppScreen.SETTINGS)
          else -> currentScreen == screen
        }

        val color = if (isSelected) CleanPrimary else TextSecondary
        val pillBg = if (isSelected) CleanPrimaryLight else Color.Transparent

        Column(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onScreenSelected(screen) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(pillBg),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = screen.title,
              tint = color,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = screen.title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = color
          )
        }
      }
    }
  }
}

@Composable
fun RebornTopHeader(
  title: String,
  subtitle: String? = null,
  streakDays: Int? = null,
  onStreakClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        letterSpacing = 0.5.sp
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = TextSecondary,
          fontWeight = FontWeight.Medium
        )
      }
    }

    if (streakDays != null && streakDays > 0) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = CleanAmberLight,
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = Modifier
          .then(if (onStreakClick != null) Modifier.clickable { onStreakClick() } else Modifier)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "🔥", fontSize = 13.sp)
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$streakDays DAYS",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = CleanAmber
          )
        }
      }
    }
  }
}
