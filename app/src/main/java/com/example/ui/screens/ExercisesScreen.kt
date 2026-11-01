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
import com.example.data.model.Difficulty
import com.example.data.model.Exercise
import com.example.data.model.ExerciseCategory
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
  viewModel: RebornViewModel,
  exercises: List<Exercise>
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }
  var selectedExerciseDetail by remember { mutableStateOf<Exercise?>(null) }
  var showCreateDialog by remember { mutableStateOf(false) }

  val filteredExercises = exercises.filter { ex ->
    val matchesSearch = ex.name.contains(searchQuery, ignoreCase = true) ||
      ex.targetMuscles.contains(searchQuery, ignoreCase = true)
    val matchesCategory = selectedCategory == null || ex.category == selectedCategory
    matchesSearch && matchesCategory
  }

  Box(modifier = Modifier.fillMaxSize().background(CyberBackground)) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      item {
        RebornTopHeader(
          title = "EXERCISE DATABASE",
          subtitle = "TECHNIQUE, MODIFICATIONS & MUSCLE TARGETS"
        )
      }

      // Search & Create Row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search exercises or muscles...", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = CyberCardBorder,
              focusedContainerColor = CyberSurface,
              unfocusedContainerColor = CyberSurface,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(NeonCyan.copy(alpha = 0.15f))
              .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Custom Exercise", tint = NeonCyan)
          }
        }
      }

      // Categories Horizontal Scroll
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            FilterChip(
              selected = selectedCategory == null,
              onClick = { selectedCategory = null },
              label = { Text("All") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                selectedLabelColor = NeonCyan,
                containerColor = CyberSurface,
                labelColor = TextSecondary
              )
            )
          }
          items(ExerciseCategory.values()) { category ->
            FilterChip(
              selected = selectedCategory == category,
              onClick = { selectedCategory = if (selectedCategory == category) null else category },
              label = { Text(category.displayName) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                selectedLabelColor = NeonCyan,
                containerColor = CyberSurface,
                labelColor = TextSecondary
              )
            )
          }
        }
      }

      // Exercise List
      items(filteredExercises, key = { it.id }) { exercise ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          ExerciseCard(
            exercise = exercise,
            onClick = { selectedExerciseDetail = exercise }
          )
        }
      }
    }

    // Exercise Detail Bottom Sheet Modal
    selectedExerciseDetail?.let { ex ->
      ModalBottomSheet(
        onDismissRequest = { selectedExerciseDetail = null },
        containerColor = CyberSurface,
        scrimColor = VoidBlack.copy(alpha = 0.7f)
      ) {
        ExerciseDetailContent(
          exercise = ex,
          onClose = { selectedExerciseDetail = null }
        )
      }
    }

    // Custom Exercise Dialog
    if (showCreateDialog) {
      CustomExerciseDialog(
        onDismiss = { showCreateDialog = false },
        onCreate = { newName, muscle, diff, sets ->
          showCreateDialog = false
        }
      )
    }
  }
}

@Composable
fun ExerciseCard(
  exercise: Exercise,
  onClick: () -> Unit
) {
  CyberCard(
    modifier = Modifier.clickable { onClick() },
    borderColor = CyberCardBorder
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = exercise.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (exercise.difficulty) {
              Difficulty.BEGINNER -> NeonGreen.copy(alpha = 0.15f)
              Difficulty.INTERMEDIATE -> NeonCyan.copy(alpha = 0.15f)
              Difficulty.ADVANCED -> NeonCrimson.copy(alpha = 0.15f)
            }
          ) {
            Text(
              text = exercise.difficulty.name,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = when (exercise.difficulty) {
                Difficulty.BEGINNER -> NeonGreen
                Difficulty.INTERMEDIATE -> NeonCyan
                Difficulty.ADVANCED -> NeonCrimson
              },
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "🎯 ${exercise.targetMuscles}",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Details",
        tint = NeonCyan
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "⚡ ${exercise.defaultSetsReps}",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = NeonGreen
      )
      Text(
        text = "⏱️ ${exercise.restSeconds}s rest",
        fontSize = 12.sp,
        color = TextTertiary
      )
    }
  }
}

@Composable
fun ExerciseDetailContent(
  exercise: Exercise,
  onClose: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 10.dp),
    contentPadding = PaddingValues(bottom = 40.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = exercise.name.uppercase(),
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan
        )
        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = CyberSurfaceVariant
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = "TARGET MUSCLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
          Text(text = exercise.targetMuscles, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = "EQUIPMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
          Text(text = exercise.equipment, fontSize = 13.sp, color = NeonGreen)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(14.dp))
      Text(text = "STEP-BY-STEP INSTRUCTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
      Spacer(modifier = Modifier.height(6.dp))
      exercise.instructions.forEachIndexed { index, step ->
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
          Text(text = "${index + 1}. ", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
          Text(text = step, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(14.dp))
      CyberCard(borderColor = NeonCrimson.copy(alpha = 0.35f)) {
        Text(text = "⚠️ COMMON MISTAKES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCrimson)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = exercise.commonMistakes, fontSize = 12.sp, color = TextSecondary)
      }
    }

    item {
      Spacer(modifier = Modifier.height(10.dp))
      CyberCard(borderColor = NeonPurple.copy(alpha = 0.35f)) {
        Text(text = "💡 BEGINNER MODIFICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = exercise.beginnerModification, fontSize = 12.sp, color = TextSecondary)
      }
    }
  }
}

@Composable
fun CustomExerciseDialog(
  onDismiss: () -> Unit,
  onCreate: (String, String, Difficulty, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var targetMuscle by remember { mutableStateOf("") }
  var setsReps by remember { mutableStateOf("3 Sets × 12 Reps") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create Custom Exercise", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Exercise Name") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = targetMuscle,
          onValueChange = { targetMuscle = it },
          label = { Text("Target Muscle") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = setsReps,
          onValueChange = { setsReps = it },
          label = { Text("Sets & Reps") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onCreate(name, targetMuscle, Difficulty.BEGINNER, setsReps)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
      ) {
        Text("Save Exercise")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = CyberSurface
  )
}
