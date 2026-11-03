package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Difficulty
import com.example.data.model.Exercise
import com.example.data.model.ExerciseCategory
import com.example.ui.components.CircularWorkoutProgress
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
  viewModel: RebornViewModel,
  selectedDay: Int = 1
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: 30-Day Plan, 1: Exercise Library
  val currentSelectedDay by viewModel.selectedChallengeDay.collectAsStateWithLifecycle()
  val workoutProgressMap by viewModel.dailyWorkoutProgress.collectAsStateWithLifecycle()
  val exercisesList by viewModel.exercises.collectAsStateWithLifecycle()
  val restSecondsRemaining by viewModel.restSecondsRemaining.collectAsStateWithLifecycle()

  var selectedExerciseDetail by remember { mutableStateOf<Exercise?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }

  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CleanBackground)
      .testTag("plan_screen")
  ) {
    RebornTopHeader(
      title = "Workout & Plan",
      subtitle = "30-Day beginner roadmap & exercise library"
    )

    // Segmented Tab Switcher (Plan vs Library)
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = CleanCard,
      contentColor = CleanPrimary,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, CleanBorder, RoundedCornerShape(12.dp))
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text(
            text = "30-Day Workout Plan",
            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
          )
        },
        selectedContentColor = CleanPrimary,
        unselectedContentColor = TextSecondary,
        modifier = Modifier.testTag("tab_30_day_plan")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = "Exercise Library",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
          )
        },
        selectedContentColor = CleanPrimary,
        unselectedContentColor = TextSecondary,
        modifier = Modifier.testTag("tab_exercise_library")
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (selectedTab == 0) {
      // 30-Day Plan Tab
      PlanTabContent(
        viewModel = viewModel,
        currentDay = currentSelectedDay,
        workoutProgressMap = workoutProgressMap,
        restSeconds = restSecondsRemaining,
        onSelectExercise = { ex -> selectedExerciseDetail = ex }
      )
    } else {
      // Exercise Library Tab
      ExerciseLibraryTabContent(
        exercises = exercisesList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedCategory = selectedCategory,
        onCategorySelect = { selectedCategory = it },
        onSelectExercise = { ex -> selectedExerciseDetail = ex }
      )
    }
  }

  // Exercise Detail Bottom Sheet with Video Demo
  selectedExerciseDetail?.let { ex ->
    ModalBottomSheet(
      onDismissRequest = { selectedExerciseDetail = null },
      containerColor = CleanCard,
      scrimColor = Color.Black.copy(alpha = 0.35f)
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 36.dp)
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = ex.name,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            IconButton(onClick = { selectedExerciseDetail = null }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
            }
          }
        }

        // Target Muscles & Category Card
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = CleanCardVariant,
            border = BorderStroke(1.dp, CleanBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = "CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                  Text(text = ex.category.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CleanPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(text = "DIFFICULTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                  Text(text = ex.difficulty.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = "TARGET MUSCLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
              Text(text = ex.targetMuscles, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
              Spacer(modifier = Modifier.height(6.dp))
              Text(text = "EQUIPMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
              Text(text = ex.equipment, fontSize = 13.sp, color = TextSecondary)
            }
          }
        }

        // Verified Video Demo Button
        if (ex.videoUrl != null) {
          item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                try {
                  uriHandler.openUri(ex.videoUrl)
                } catch (e: Exception) {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ex.videoUrl))
                  context.startActivity(intent)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_watch_demo_video"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary)
            ) {
              Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Watch Video Demonstration", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }

        // Step by step instructions
        item {
          Spacer(modifier = Modifier.height(16.dp))
          Text(text = "Instructions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(8.dp))
          ex.instructions.forEachIndexed { idx, step ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
              Text(
                text = "${idx + 1}. ",
                fontWeight = FontWeight.Bold,
                color = CleanPrimary,
                fontSize = 13.sp
              )
              Text(
                text = step,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )
            }
          }
        }

        // Common Mistakes
        item {
          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, Color(0xFFFECACA))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(text = "⚠️ Common Mistakes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = ex.commonMistakes, fontSize = 13.sp, color = TextSecondary)
            }
          }
        }

        // Beginner Modification
        item {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = CleanPrimaryLight,
            border = BorderStroke(1.dp, CleanBorder)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(text = "💡 Beginner Modification", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CleanPrimaryDark)
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = ex.beginnerModification, fontSize = 13.sp, color = TextSecondary)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PlanTabContent(
  viewModel: RebornViewModel,
  currentDay: Int,
  workoutProgressMap: Map<Int, com.example.data.model.DailyWorkoutProgress>,
  restSeconds: Int,
  onSelectExercise: (Exercise) -> Unit
) {
  val dayWorkout = remember(currentDay) { viewModel.getWorkoutForDay(currentDay) }
  val progress = workoutProgressMap[currentDay] ?: remember(currentDay) {
    viewModel.getWorkoutProgressForDay(currentDay)
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 30-Day Horizontal Day Picker
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
      ) {
        items((1..30).toList()) { day ->
          val isSelected = day == currentDay
          val dayProg = workoutProgressMap[day] ?: viewModel.getWorkoutProgressForDay(day)
          val isDone = dayProg.isCompleted

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
              isSelected -> CleanPrimary
              isDone -> CleanPrimaryLight
              else -> CleanCard
            },
            border = BorderStroke(
              1.dp,
              when {
                isSelected -> CleanPrimaryDark
                isDone -> CleanPrimary
                else -> CleanBorder
              }
            ),
            modifier = Modifier
              .clickable { viewModel.selectChallengeDay(day) }
              .testTag("day_selector_$day")
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Day $day",
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                  isSelected -> Color.White
                  isDone -> CleanPrimaryDark
                  else -> TextPrimary
                }
              )
              Text(
                text = if (isDone) "Done" else "${dayProg.percentage}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                  isSelected -> Color.White.copy(alpha = 0.9f)
                  isDone -> CleanPrimaryDark
                  else -> TextSecondary
                }
              )
            }
          }
        }
      }
    }

    // Selected Day Overview Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "DAY $currentDay OF 30",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CleanPrimary,
                letterSpacing = 0.6.sp
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = dayWorkout.title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (dayWorkout.isRestDay) "Active Recovery & Rest Day"
                else "Focus: ${dayWorkout.focus} • ${dayWorkout.estimatedMinutes} mins • ${dayWorkout.exercises.size} Exercises",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            CircularWorkoutProgress(
              percentage = progress.percentage,
              size = 64.dp,
              strokeWidth = 6.dp,
              label = if (progress.isCompleted) "Done" else "${progress.completedExercises}/${progress.totalExercises}"
            )
          }

          if (dayWorkout.isRestDay) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = CleanPrimaryLight,
              border = BorderStroke(1.dp, CleanBorder)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = CleanPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Today is an assigned recovery day. Hydrate, take an easy walk, and allow your muscles to rebuild.",
                  fontSize = 12.sp,
                  color = CleanPrimaryDark,
                  lineHeight = 17.sp
                )
              }
            }
          }
        }
      }
    }

    // Rest Timer Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
          .testTag("card_rest_timer"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Timer, contentDescription = null, tint = CleanPrimary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Rest Timer",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }
            Text(
              text = if (restSeconds > 0) "${restSeconds}s remaining" else "Ready",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (restSeconds > 0) CleanPrimary else TextSecondary
            )
          }

          if (restSeconds > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { (restSeconds / 90f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = CleanPrimary,
              trackColor = Color(0xFFE2E8F0)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(30, 60, 90).forEach { sec ->
              OutlinedButton(
                onClick = { viewModel.startRestTimer(sec) },
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CleanBorder),
                contentPadding = PaddingValues(0.dp)
              ) {
                Text("${sec}s", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
              }
            }

            if (restSeconds > 0) {
              Button(
                onClick = { viewModel.stopRestTimer() },
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                contentPadding = PaddingValues(0.dp)
              ) {
                Text("Stop", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // Assigned Exercises List with Checkboxes
    if (dayWorkout.exercises.isNotEmpty()) {
      item {
        Text(
          text = "TODAY'S EXERCISES (${progress.completedExercises}/${progress.totalExercises})",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.6.sp
        )
      }

      items(dayWorkout.exercises) { ex ->
        val isDone = progress.completedExerciseIds.contains(ex.id)

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .border(
              1.dp,
              if (isDone) CleanPrimary.copy(alpha = 0.4f) else CleanBorder,
              RoundedCornerShape(12.dp)
            ),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFFF0FDF4) else CleanCard
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = isDone,
              onCheckedChange = { viewModel.toggleExerciseInWorkout(currentDay, ex.id) },
              colors = CheckboxDefaults.colors(
                checkedColor = CleanPrimary,
                uncheckedColor = Color(0xFF94A3B8)
              ),
              modifier = Modifier
                .size(24.dp)
                .testTag("checkbox_ex_${ex.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
              modifier = Modifier
                .weight(1f)
                .clickable { onSelectExercise(ex) }
            ) {
              Text(
                text = ex.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) TextSecondary else TextPrimary,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${ex.defaultSetsReps} • Rest ${ex.restSeconds}s",
                fontSize = 12.sp,
                color = CleanPrimaryDark,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "Target: ${ex.targetMuscles}",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }

            IconButton(
              onClick = { onSelectExercise(ex) },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Details",
                tint = CleanPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ExerciseLibraryTabContent(
  exercises: List<Exercise>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedCategory: ExerciseCategory?,
  onCategorySelect: (ExerciseCategory?) -> Unit,
  onSelectExercise: (Exercise) -> Unit
) {
  val filtered = exercises.filter { ex ->
    val matchesSearch = ex.name.contains(searchQuery, ignoreCase = true) ||
      ex.targetMuscles.contains(searchQuery, ignoreCase = true)
    val matchesCategory = selectedCategory == null || ex.category == selectedCategory
    matchesSearch && matchesCategory
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Search input
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search by exercise or target muscle...", color = TextSecondary, fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = CleanPrimary)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_exercises"),
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
    }

    // 7 Required Categories Horizontal Filter
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
      ) {
        item {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelect(null) },
            label = { Text("All", fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CleanPrimaryLight,
              selectedLabelColor = CleanPrimaryDark,
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

        items(ExerciseCategory.values()) { cat ->
          val isSel = selectedCategory == cat
          FilterChip(
            selected = isSel,
            onClick = { onCategorySelect(if (isSel) null else cat) },
            label = { Text(cat.displayName, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CleanPrimaryLight,
              selectedLabelColor = CleanPrimaryDark,
              containerColor = CleanCard,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = CleanBorder,
              selectedBorderColor = CleanPrimary,
              enabled = true,
              selected = isSel
            )
          )
        }
      }
    }

    // Exercise Cards
    items(filtered) { ex ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .clickable { onSelectExercise(ex) }
          .border(1.dp, CleanBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = ex.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "🎯 ${ex.targetMuscles}",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = when (ex.difficulty) {
                Difficulty.BEGINNER -> CleanEmeraldLight
                Difficulty.INTERMEDIATE -> Color(0xFFEFF6FF)
                Difficulty.ADVANCED -> Color(0xFFFEF2F2)
              }
            ) {
              Text(
                text = ex.difficulty.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when (ex.difficulty) {
                  Difficulty.BEGINNER -> CleanPrimary
                  Difficulty.INTERMEDIATE -> Color(0xFF2563EB)
                  Difficulty.ADVANCED -> Color(0xFFDC2626)
                },
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "⚡ ${ex.defaultSetsReps}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = CleanPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (ex.videoUrl != null) {
                Icon(
                  Icons.Default.PlayCircle,
                  contentDescription = null,
                  tint = CleanPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Video Demo",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = CleanPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
              }
              Text(
                text = "⏱️ ${ex.restSeconds}s rest",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
          }
        }
      }
    }
  }
}
