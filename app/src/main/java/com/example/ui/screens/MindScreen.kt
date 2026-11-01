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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MindLog
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun MindScreen(
  viewModel: RebornViewModel,
  mindLog: MindLog
) {
  var mood by remember { mutableStateOf(mindLog.mood) }
  var focusScore by remember { mutableStateOf(mindLog.focusScore.toFloat()) }
  var confidenceScore by remember { mutableStateOf(mindLog.confidenceScore.toFloat()) }
  var meditationMinutes by remember { mutableStateOf(mindLog.meditationMinutes.toString()) }
  var readingPages by remember { mutableStateOf(mindLog.readingPages.toString()) }
  var journalText by remember { mutableStateOf(mindLog.journalText) }
  var reflectionText by remember { mutableStateOf(mindLog.reflectionText) }

  val moods = listOf("Focused", "Stoic", "Energized", "Calm", "Fatigued")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "MIND FORTRESS",
        subtitle = "FOCUS, CONFIDENCE, MEDITATION & JOURNALING"
      )
    }

    // Mood Selector
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = NeonPurple.copy(alpha = 0.4f)
      ) {
        Text(
          text = "TODAY'S MENTAL STATE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = NeonPurple,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(moods) { m ->
            val isSelected = mood.equals(m, ignoreCase = true)
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) NeonPurple.copy(alpha = 0.25f) else CyberSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) NeonPurple else CyberCardBorder
              ),
              modifier = Modifier.clickable { mood = m }
            ) {
              Text(
                text = m,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NeonPurple else TextSecondary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }
          }
        }
      }
    }

    // Sliders: Focus & Confidence (1 to 10)
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CyberCardBorder
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "FOCUS LEVEL (1 - 10)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Text(
            text = "${focusScore.toInt()} / 10",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan
          )
        }
        Slider(
          value = focusScore,
          onValueChange = { focusScore = it },
          valueRange = 1f..10f,
          steps = 8,
          colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "CONFIDENCE LEVEL (1 - 10)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Text(
            text = "${confidenceScore.toInt()} / 10",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = NeonPurple
          )
        }
        Slider(
          value = confidenceScore,
          onValueChange = { confidenceScore = it },
          valueRange = 1f..10f,
          steps = 8,
          colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
        )
      }
    }

    // Meditation & Reading Inputs
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CyberCardBorder
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = meditationMinutes,
            onValueChange = { meditationMinutes = it },
            label = { Text("Meditation (Mins)") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = readingPages,
            onValueChange = { readingPages = it },
            label = { Text("Reading (Pages)") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // Journal & Reflection
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CyberCardBorder
      ) {
        Text(text = "DAILY JOURNALING & LOG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = journalText,
          onValueChange = { journalText = it },
          placeholder = { Text("What did you conquer today? Any internal resistance?", color = TextTertiary, fontSize = 12.sp) },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "DAILY SELF-REFLECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = reflectionText,
          onValueChange = { reflectionText = it },
          placeholder = { Text("How was your eye contact, discipline, and presence?", color = TextTertiary, fontSize = 12.sp) },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          shape = RoundedCornerShape(12.dp)
        )
      }
    }

    // Save CTA Button
    item {
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Button(
          onClick = {
            viewModel.saveMindLog(
              MindLog(
                date = "Today",
                mood = mood,
                focusScore = focusScore.toInt(),
                confidenceScore = confidenceScore.toInt(),
                meditationMinutes = meditationMinutes.toIntOrNull() ?: 0,
                readingPages = readingPages.toIntOrNull() ?: 0,
                journalText = journalText,
                reflectionText = reflectionText
              )
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(vertical = 12.dp)
        ) {
          Text("SAVE MIND FORTRESS (+30 XP)", fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
      }
    }
  }
}
