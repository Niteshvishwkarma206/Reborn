package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.StepSource
import com.example.data.repository.RebornRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("com.aistudio.projectreborn.rpgx")
          .setProjectId("demo-reborn-project")
          .setApiKey("test-api-key")
          .build()
      )
    }
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("reborn", appName)
  }

  @Test
  fun `workout progress calculates exact percentage and completes on all done`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = RebornRepository(context)

    // Day 1 has 4 exercises: arm circles, squats, wall push-ups, plank
    val day1 = repository.getWorkoutForDay(1)
    assertEquals(4, day1.exercises.size)
    assertFalse(day1.isRestDay)

    val ex1 = day1.exercises[0].id
    val ex2 = day1.exercises[1].id
    val ex3 = day1.exercises[2].id
    val ex4 = day1.exercises[3].id

    // Complete 1 of 4 -> 25%
    val prog1 = repository.toggleExerciseInWorkout(1, ex1)
    assertEquals(25, prog1.percentage)
    assertEquals(1, prog1.completedExercises)
    assertFalse(prog1.isCompleted)

    // Complete 2nd and 3rd -> 3 of 4 = 75%
    repository.toggleExerciseInWorkout(1, ex2)
    val prog3 = repository.toggleExerciseInWorkout(1, ex3)
    assertEquals(75, prog3.percentage)
    assertEquals(3, prog3.completedExercises)
    assertFalse(prog3.isCompleted)

    // Complete 4th -> 4 of 4 = 100% and isCompleted is true
    val prog4 = repository.toggleExerciseInWorkout(1, ex4)
    assertEquals(100, prog4.percentage)
    assertEquals(4, prog4.completedExercises)
    assertTrue(prog4.isCompleted)

    // Uncheck 1 exercise -> 3 of 4 = 75%, isCompleted is false
    val progUncheck = repository.toggleExerciseInWorkout(1, ex4)
    assertEquals(75, progUncheck.percentage)
    assertEquals(3, progUncheck.completedExercises)
    assertFalse(progUncheck.isCompleted)
  }

  @Test
  fun `rest days return completed status and do not penalize progress`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = RebornRepository(context)

    // Day 4 is an assigned Rest & Active Recovery Day
    val day4 = repository.getWorkoutForDay(4)
    assertTrue(day4.isRestDay)

    val prog4 = repository.getWorkoutProgressForDay(4)
    assertTrue(prog4.isCompleted)
    assertEquals(100, prog4.percentage)
  }

  @Test
  fun `manual step entry records steps and distance accurately`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = RebornRepository(context)

    repository.recordManualSteps(8000, "Today")
    val state = repository.stepTrackerState.value

    assertEquals(8000, state.todaySteps)
    assertEquals(StepSource.MANUAL, state.source)
    assertTrue(state.distanceKm > 5.0f) // 8000 * 0.00075 = 6.0 km
  }
}
