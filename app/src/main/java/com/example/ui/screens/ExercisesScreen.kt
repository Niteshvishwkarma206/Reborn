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
          title = "Exercise Library",
          subtitle = "Technique, modifications & target muscles"
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
            placeholder = { Text("Search exercises or muscles...", color = TextSecondary, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = CleanPrimary)
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CleanPrimary,
              unfocusedBorderColor = CleanBorder,
              focusedContainerColor = CleanCard,
              unfocusedContainerColor = CleanCard,
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
              .background(CleanPrimary.copy(alpha = 0.12f))
              .border(1.dp, CleanPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Custom Exercise", tint = CleanPrimary)
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
              label = { Text("All", fontWeight = if (selectedCategory == null) FontWeight.SemiBold else FontWeight.Normal) },
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
                selected = selectedCategory == null
              )
            )
          }
          items(ExerciseCategory.values()) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
              selected = isSelected,
              onClick = { selectedCategory = if (isSelected) null else category },
              label = { Text(category.displayName, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
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
        containerColor = CleanCard,
        scrimColor = Color.Black.copy(alpha = 0.35f)
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
    borderColor = CleanBorder
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
          val (badgeBg, badgeText) = when (exercise.difficulty) {
            Difficulty.BEGINNER -> Pair(Color(0xFFECFDF5), CleanPrimary)
            Difficulty.INTERMEDIATE -> Pair(Color(0xFFEFF6FF), Color(0xFF2563EB))
            Difficulty.ADVANCED -> Pair(Color(0xFFFEF2F2), Color(0xFFDC2626))
          }
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = badgeBg
          ) {
            Text(
              text = exercise.difficulty.name,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = badgeText,
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
        tint = TextSecondary
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
        color = CleanPrimary
      )
      Text(
        text = "⏱️ ${exercise.restSeconds}s rest",
        fontSize = 12.sp,
        color = TextSecondary
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
          text = exercise.name,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = CleanCardVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = "TARGET MUSCLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          Text(text = exercise.targetMuscles, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "EQUIPMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          Text(text = exercise.equipment, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = CleanPrimary)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = "Step-by-step Instructions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
      Spacer(modifier = Modifier.height(8.dp))
      exercise.instructions.forEachIndexed { index, step ->
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
          Text(text = "${index + 1}. ", fontWeight = FontWeight.Bold, color = CleanPrimary, fontSize = 13.sp)
          Text(text = step, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(14.dp))
      CyberCard(borderColor = Color(0xFFFCA5A5)) {
        Text(text = "⚠️ Common Mistakes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = exercise.commonMistakes, fontSize = 13.sp, color = TextSecondary)
      }
    }

    item {
      Spacer(modifier = Modifier.height(10.dp))
      CyberCard(borderColor = CleanBorder) {
        Text(text = "💡 Beginner Modification", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CleanPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = exercise.beginnerModification, fontSize = 13.sp, color = TextSecondary)
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
        colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary, contentColor = Color.White)
      ) {
        Text("Save Exercise")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = CleanCard
  )
}
