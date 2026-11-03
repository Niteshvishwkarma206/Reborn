package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalityTask
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun PersonalityScreen(
  viewModel: RebornViewModel,
  tasks: List<PersonalityTask>
) {
  var selectedDomain by remember { mutableStateOf<String?>(null) }

  val domains = listOf(
    "Communication",
    "Public Speaking",
    "Eye Contact",
    "Body Language",
    "Confidence",
    "Grooming",
    "Social Interaction",
    "Discipline"
  )

  val filteredTasks = if (selectedDomain == null) {
    tasks
  } else {
    tasks.filter { it.domain.equals(selectedDomain, ignoreCase = true) }
  }

  val completedCount = tasks.count { it.isCompleted }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "Personality & Growth",
        subtitle = "Presence, communication & social discipline"
      )
    }

    // Persona Mastery Overview Card
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CleanBorder
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "GROWTH & DISCIPLINE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "$completedCount / ${tasks.size} Drills Completed",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFECFDF5)
          ) {
            Text(
              text = "+${completedCount * 40} XP EARNED",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = CleanPrimary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // Domains Horizontal Filter
    item {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          FilterChip(
            selected = selectedDomain == null,
            onClick = { selectedDomain = null },
            label = { Text("All Domains", fontWeight = if (selectedDomain == null) FontWeight.SemiBold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CleanPrimary.copy(alpha = 0.15f),
              selectedLabelColor = CleanPrimary,
              containerColor = CleanCard,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = CleanBorder,
              selectedBorderColor = CleanPrimary,
              enabled = true,
              selected = selectedDomain == null
            )
          )
        }
        items(domains) { domain ->
          val isSelected = selectedDomain.equals(domain, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = {
              selectedDomain = if (isSelected) null else domain
            },
            label = { Text(domain, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CleanPrimary.copy(alpha = 0.15f),
              selectedLabelColor = CleanPrimary,
              containerColor = CleanCard,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = CleanBorder,
              selectedBorderColor = CleanPrimary,
              enabled = true,
              selected = isSelected
            )
          )
        }
      }
    }

    // Tasks Checklist
    items(filteredTasks, key = { it.id }) { task ->
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
        PersonalityTaskCard(
          task = task,
          onToggle = { viewModel.togglePersonalityTask(task.id) }
        )
      }
    }
  }
}

@Composable
fun PersonalityTaskCard(
  task: PersonalityTask,
  onToggle: () -> Unit
) {
  CyberCard(
    modifier = Modifier.clickable { onToggle() },
    borderColor = if (task.isCompleted) CleanPrimary.copy(alpha = 0.5f) else CleanBorder
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(if (task.isCompleted) CleanPrimary else Color.Transparent)
          .border(
            1.5.dp,
            if (task.isCompleted) CleanPrimary else CleanBorder,
            RoundedCornerShape(6.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        if (task.isCompleted) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
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
              text = task.domain.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Text(
            text = "+${task.xp} XP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CleanPrimary
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = task.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (task.isCompleted) TextSecondary else TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = task.instructions,
          fontSize = 12.sp,
          color = TextSecondary,
          lineHeight = 17.sp
        )
      }
    }
  }
}
