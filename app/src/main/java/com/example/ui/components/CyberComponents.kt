package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@Composable
fun CyberCard(
  modifier: Modifier = Modifier,
  borderColor: Color = CyberCardBorder,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CyberSurface)
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
fun XpProgressBar(
  level: Int,
  currentXp: Int,
  xpToNextLevel: Int,
  rankTitle: String,
  modifier: Modifier = Modifier
) {
  val progress = (currentXp.toFloat() / xpToNextLevel.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
  CyberCard(
    modifier = modifier,
    borderColor = NeonGreen.copy(alpha = 0.5f)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(NeonGreen, NeonGreenBright)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "LV.$level",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = VoidBlack
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = rankTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
          )
          Text(
            text = "RANK STATUS: ACTIVE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeonGreen
          )
        }
      }
      Text(
        text = "$currentXp / $xpToNextLevel XP",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = NeonGreen
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Segmented neon progress bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
        .clip(RoundedCornerShape(5.dp))
        .background(Color(0xFF1E2433))
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(progress)
          .clip(RoundedCornerShape(5.dp))
          .background(
            Brush.horizontalGradient(
              listOf(NeonGreen, NeonGreenBright)
            )
          )
      )
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
      .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CyberSurface)
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
          letterSpacing = 0.8.sp
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = accentColor,
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
    AppScreen.CHALLENGE to Icons.Default.Flag,
    AppScreen.WORKOUT to Icons.Default.FitnessCenter,
    AppScreen.WALKING to Icons.Default.DirectionsWalk,
    AppScreen.NUTRITION to Icons.Default.Restaurant,
    AppScreen.SETTINGS to Icons.Default.Person
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars),
    color = VoidBlack,
    tonalElevation = 8.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder.copy(alpha = 0.6f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp, horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      primaryScreens.forEach { (screen, icon) ->
        val isSelected = currentScreen == screen
        val color = if (isSelected) NeonGreen else TextTertiary
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
              .background(if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.Transparent),
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
            fontSize = 10.sp,
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
        text = title.uppercase(),
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        color = TextPrimary,
        letterSpacing = 1.sp
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    if (streakDays != null) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = CyberSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
        modifier = Modifier
          .then(if (onStreakClick != null) Modifier.clickable { onStreakClick() } else Modifier)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "🔥", fontSize = 14.sp)
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$streakDays DAYS",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = NeonAmber
          )
        }
      }
    }
  }
}
