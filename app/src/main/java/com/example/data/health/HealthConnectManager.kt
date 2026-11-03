package com.example.data.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.data.model.DailyStepCount
import com.example.data.model.StepSource
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class HealthConnectManager(private val context: Context) {

  private val providerPackageName = "com.google.android.apps.healthdata"

  val requiredPermissions = setOf(
    HealthPermission.getReadPermission(StepsRecord::class)
  )

  fun getAvailabilityStatus(): Int {
    return try {
      HealthConnectClient.getSdkStatus(context, providerPackageName)
    } catch (e: Exception) {
      Log.w("HealthConnect", "Failed to check SDK status: ${e.message}")
      HealthConnectClient.SDK_UNAVAILABLE
    }
  }

  fun isAvailable(): Boolean {
    return getAvailabilityStatus() == HealthConnectClient.SDK_AVAILABLE
  }

  fun getClient(): HealthConnectClient? {
    return try {
      if (isAvailable()) {
        HealthConnectClient.getOrCreate(context)
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w("HealthConnect", "Failed to get HealthConnectClient: ${e.message}")
      null
    }
  }

  suspend fun hasPermissions(): Boolean {
    val client = getClient() ?: return false
    return try {
      val granted = client.permissionController.getGrantedPermissions()
      granted.containsAll(requiredPermissions)
    } catch (e: Exception) {
      Log.w("HealthConnect", "Error checking permissions: ${e.message}")
      false
    }
  }

  suspend fun readTodaySteps(): Long {
    val client = getClient() ?: return 0L
    return try {
      val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
      val now = Instant.now()
      val response = client.aggregate(
        AggregateRequest(
          metrics = setOf(StepsRecord.COUNT_TOTAL),
          timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
        )
      )
      response[StepsRecord.COUNT_TOTAL] ?: 0L
    } catch (e: Exception) {
      Log.w("HealthConnect", "Error reading today's steps: ${e.message}")
      0L
    }
  }

  suspend fun readWeeklySteps(goal: Int): List<DailyStepCount> {
    val client = getClient() ?: return emptyList()
    val list = mutableListOf<DailyStepCount>()
    val today = LocalDate.now()
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    for (i in 0..6) {
      val date = monday.plusDays(i.toLong())
      val dayName = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
      if (date.isAfter(today)) {
        list.add(
          DailyStepCount(
            dayOfWeek = dayName,
            date = date.toString(),
            steps = 0,
            goal = goal,
            source = StepSource.HEALTH_CONNECT
          )
        )
        continue
      }
      try {
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end = if (date == today) Instant.now() else date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val response = client.aggregate(
          AggregateRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(start, end)
          )
        )
        val steps = (response[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
        list.add(
          DailyStepCount(
            dayOfWeek = dayName,
            date = date.toString(),
            steps = steps,
            goal = goal,
            source = StepSource.HEALTH_CONNECT
          )
        )
      } catch (e: Exception) {
        list.add(
          DailyStepCount(
            dayOfWeek = dayName,
            date = date.toString(),
            steps = 0,
            goal = goal,
            source = StepSource.HEALTH_CONNECT
          )
        )
      }
    }
    return list
  }
}
