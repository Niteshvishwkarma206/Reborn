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
        title = "Mind & Focus",
        subtitle = "Meditation, focus, confidence & daily reflection"
      )
    }

    // Mood Selector
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CleanBorder
      ) {
        Text(
          text = "TODAY'S MENTAL STATE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(moods) { m ->
            val isSelected = mood.equals(m, ignoreCase = true)
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) CleanPrimary.copy(alpha = 0.15f) else CleanCardVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) CleanPrimary else CleanBorder
              ),
              modifier = Modifier.clickable { mood = m }
            ) {
              Text(
                text = m,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CleanPrimary else TextSecondary,
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
        borderColor = CleanBorder
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Focus Level (1 - 10)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
          Text(
            text = "${focusScore.toInt()} / 10",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CleanPrimary
          )
        }
        Slider(
          value = focusScore,
          onValueChange = { focusScore = it },
          valueRange = 1f..10f,
          steps = 8,
          colors = SliderDefaults.colors(
            thumbColor = CleanPrimary,
            activeTrackColor = CleanPrimary,
            inactiveTrackColor = CleanBorder
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Confidence Level (1 - 10)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
          Text(
            text = "${confidenceScore.toInt()} / 10",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0284C7)
          )
        }
        Slider(
          value = confidenceScore,
          onValueChange = { confidenceScore = it },
          valueRange = 1f..10f,
          steps = 8,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFF0284C7),
            activeTrackColor = Color(0xFF0284C7),
            inactiveTrackColor = CleanBorder
          )
        )
      }
    }

    // Meditation & Reading Inputs
    item {
      CyberCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        borderColor = CleanBorder
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
        borderColor = CleanBorder
      ) {
        Text(text = "Daily Journaling", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = journalText,
          onValueChange = { journalText = it },
          placeholder = { Text("What did you conquer today? Any internal thoughts?", color = TextSecondary, fontSize = 12.sp) },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Daily Self-Reflection", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = reflectionText,
          onValueChange = { reflectionText = it },
          placeholder = { Text("Discipline, presence, and personal growth notes...", color = TextSecondary, fontSize = 12.sp) },
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
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary, contentColor = Color.White),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(vertical = 12.dp)
        ) {
          Text("SAVE MIND LOG", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
