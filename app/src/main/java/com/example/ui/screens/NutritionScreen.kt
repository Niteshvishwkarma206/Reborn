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
import com.example.data.model.MealItem
import com.example.data.model.NutritionEntry
import com.example.data.model.UserProfile
import com.example.ui.components.CyberCard
import com.example.ui.components.RebornTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RebornViewModel

@Composable
fun NutritionScreen(
  viewModel: RebornViewModel,
  user: UserProfile,
  nutrition: NutritionEntry
) {
  var showAddMealDialog by remember { mutableStateOf(false) }
  var activeMealType by remember { mutableStateOf("Breakfast") }

  val allMeals = listOf(
    "Breakfast" to nutrition.breakfast,
    "Lunch" to nutrition.lunch,
    "Evening Snack" to nutrition.eveningSnack,
    "Dinner" to nutrition.dinner
  )

  val totalCalories = allMeals.flatMap { it.second }.sumOf { it.calories }
  val totalProtein = allMeals.flatMap { it.second }.sumOf { it.proteinGrams.toDouble() }.toFloat()

  val indianSuggestions = listOf(
    MealItem("Dal Tadka (1 Bowl)", 180, 9f),
    MealItem("Whole Wheat Roti (2 pcs)", 220, 6f),
    MealItem("Paneer Bhurji (150g)", 260, 18f),
    MealItem("Boiled Eggs (2 pcs)", 155, 13f),
    MealItem("Roasted Chana (50g)", 185, 9.5f),
    MealItem("Oats with Nuts & Milk", 310, 11f),
    MealItem("Poha with Peanuts", 250, 5.5f),
    MealItem("Upma with Veggies", 220, 5f),
    MealItem("Rajma Masala (1 Bowl)", 240, 12f),
    MealItem("Chole (1 Bowl)", 260, 11f),
    MealItem("Curd / Dahi (100g)", 98, 4f),
    MealItem("Soy Chunks Curry (100g)", 210, 22f),
    MealItem("Grilled Chicken (150g)", 245, 31f),
    MealItem("Fresh Seasonal Fruits", 110, 1.5f),
    MealItem("Steamed Basmati Rice (1 Cup)", 205, 4f)
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      RebornTopHeader(
        title = "Nutrition & Diet",
        subtitle = "Indian meal logging & water intake"
      )
    }

    // Daily Macro & Water Overview
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
              text = "TOTAL DAILY INTAKE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "$totalCalories kcal",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Estimated Protein: %.1f g".format(totalProtein),
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = CleanPrimary
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(text = "HYDRATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text(
              text = "${nutrition.waterMl} / ${user.dailyWaterTargetMl} ml",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = CleanPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Water Quick Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { viewModel.addWater(250) },
            colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary.copy(alpha = 0.12f), contentColor = CleanPrimary),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("+250ml", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          }
          Button(
            onClick = { viewModel.addWater(500) },
            colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary.copy(alpha = 0.12f), contentColor = CleanPrimary),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("+500ml", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          }
          Button(
            onClick = { viewModel.addWater(-250) },
            colors = ButtonDefaults.buttonColors(containerColor = CleanCardVariant, contentColor = TextSecondary),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("-250", fontSize = 12.sp)
          }
        }
      }
    }

    // Quick Indian Food Suggestion Bar
    item {
      Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
          text = "INDIAN MEAL SUGGESTIONS (TAP TO ADD TO LUNCH)",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          letterSpacing = 0.5.sp
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(indianSuggestions) { item ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = CleanCard,
              border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder),
              modifier = Modifier.clickable {
                viewModel.logMeal("Lunch", item)
              }
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(text = item.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${item.calories} kcal • ${item.proteinGrams}g pro", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = CleanPrimary)
              }
            }
          }
        }
      }
    }

    // Meals: Breakfast, Lunch, Snack, Dinner
    allMeals.forEach { (mealName, items) ->
      item {
        MealSectionCard(
          mealName = mealName,
          items = items,
          onAddItem = {
            activeMealType = mealName
            showAddMealDialog = true
          },
          onRemoveItem = { idx ->
            viewModel.removeMeal(mealName, idx)
          }
        )
      }
    }
  }

  if (showAddMealDialog) {
    AddCustomMealDialog(
      mealType = activeMealType,
      onDismiss = { showAddMealDialog = false },
      onSave = { name, cal, pro ->
        viewModel.logMeal(activeMealType, MealItem(name, cal, pro))
        showAddMealDialog = false
      }
    )
  }
}

@Composable
fun MealSectionCard(
  mealName: String,
  items: List<MealItem>,
  onAddItem: () -> Unit,
  onRemoveItem: (Int) -> Unit
) {
  CyberCard(
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    borderColor = CleanBorder
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = mealName,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      IconButton(onClick = onAddItem) {
        Icon(Icons.Default.AddCircle, contentDescription = "Add Item", tint = CleanPrimary)
      }
    }

    if (items.isEmpty()) {
      Text(
        text = "No items recorded. Tap '+' or choose from suggestions above.",
        fontSize = 12.sp,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 4.dp)
      )
    } else {
      items.forEachIndexed { idx, item ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = "${item.calories} kcal • ${item.proteinGrams}g protein", fontSize = 11.sp, color = TextSecondary)
          }
          IconButton(
            onClick = { onRemoveItem(idx) },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

@Composable
fun AddCustomMealDialog(
  mealType: String,
  onDismiss: () -> Unit,
  onSave: (String, Int, Float) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var calories by remember { mutableStateOf("200") }
  var protein by remember { mutableStateOf("10.0") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add to $mealType", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Food Item Name") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = calories,
          onValueChange = { calories = it },
          label = { Text("Calories (kcal)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = protein,
          onValueChange = { protein = it },
          label = { Text("Protein (grams)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val cal = calories.toIntOrNull() ?: 0
            val pro = protein.toFloatOrNull() ?: 0f
            onSave(name, cal, pro)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary, contentColor = Color.White)
      ) {
        Text("Log Food")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
    },
    containerColor = CleanCard
  )
}
