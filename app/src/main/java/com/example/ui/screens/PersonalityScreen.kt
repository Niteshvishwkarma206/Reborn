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
        title = "PERSONALITY & CHARISMA",
        subtitle = "PRESENCE, ASSERTION & SOCIAL MASTERY"
      )
    }

    // Persona Mastery Overview Card
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = NeonCrimson.copy(alpha = 0.4f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "CHARISMA & DISCIPLINE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCrimson,
              letterSpacing = 1.sp
            )
            Text(
              text = "$completedCount / ${tasks.size} Drills Executed",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = NeonCrimson.copy(alpha = 0.15f)
          ) {
            Text(
              text = "+${completedCount * 40} XP EARNED",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCrimson,
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
            label = { Text("All Domains") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NeonCrimson.copy(alpha = 0.25f),
              selectedLabelColor = NeonCrimson,
              containerColor = CyberSurface,
              labelColor = TextSecondary
            )
          )
        }
        items(domains) { domain ->
          FilterChip(
            selected = selectedDomain.equals(domain, ignoreCase = true),
            onClick = {
              selectedDomain = if (selectedDomain.equals(domain, ignoreCase = true)) null else domain
            },
            label = { Text(domain) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NeonCrimson.copy(alpha = 0.25f),
              selectedLabelColor = NeonCrimson,
              containerColor = CyberSurface,
              labelColor = TextSecondary
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
    borderColor = if (task.isCompleted) NeonGreen.copy(alpha = 0.4f) else CyberCardBorder
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(if (task.isCompleted) NeonGreen else Color.Transparent)
          .border(
            1.5.dp,
            if (task.isCompleted) NeonGreen else TextTertiary,
            RoundedCornerShape(6.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        if (task.isCompleted) {
          Icon(Icons.Default.Check, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(16.dp))
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
            color = NeonPurple.copy(alpha = 0.15f)
          ) {
            Text(
              text = task.domain.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = NeonPurple,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Text(
            text = "+${task.xp} XP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan
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
          color = TextTertiary,
          lineHeight = 17.sp
        )
      }
    }
  }
}
