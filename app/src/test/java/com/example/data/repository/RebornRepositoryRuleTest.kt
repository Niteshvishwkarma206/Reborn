package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RebornRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createProfile_validPayload_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val docRef = firestore.collection("users").document(uid)

    val payload = mapOf(
      "userId" to uid,
      "name" to "Alice Hunter",
      "level" to 1,
      "currentXp" to 100,
      "currentChallengeDay" to 1
    )

    withTimeout(DEFAULT_TIMEOUT_MS) {
      docRef.set(payload).await()
    }

    val snapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
      docRef.get().await()
    }
    assertTrue(snapshot.exists())
    assertEquals("Alice Hunter", snapshot.getString("name"))
  }

  @Test
  fun completeMission_authenticatedOwner_syncsToFirestore() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val missionRef = firestore.collection("users").document(uid).collection("missions").document("m1_1")

    val missionData = mapOf(
      "id" to "m1_1",
      "day" to 1,
      "title" to "Day 1 Awakening",
      "category" to "WALKING",
      "xpReward" to 50,
      "isCompleted" to true
    )

    withTimeout(DEFAULT_TIMEOUT_MS) {
      missionRef.set(missionData).await()
    }

    val snapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
      missionRef.get().await()
    }
    assertTrue(snapshot.exists())
    assertEquals(true, snapshot.getBoolean("isCompleted"))
  }

  @Test
  fun crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceDoc = firestore.collection("users").document(aliceUid)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceDoc.set(
        mapOf(
          "userId" to aliceUid,
          "name" to "Alice",
          "level" to 1,
          "currentXp" to 0,
          "currentChallengeDay" to 1
        )
      ).await()
    }

    // Sign in as Bob and attempt to read Alice's record
    signInTestUser(BOB_EMAIL)
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        aliceDoc.get().await()
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: Exception) {
      val fsEx = e.unwrapFirestoreException()
      assertTrue(fsEx != null)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }
  }

  @Test
  fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val docRef = firestore.collection("users").document(aliceUid)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      docRef.set(
        mapOf(
          "userId" to aliceUid,
          "name" to "Alice",
          "level" to 1,
          "currentXp" to 0,
          "currentChallengeDay" to 1
        )
      ).await()
    }

    // Sign out to simulate unauthenticated caller
    auth.signOut()
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        docRef.get().await()
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: Exception) {
      val fsEx = e.unwrapFirestoreException()
      assertTrue(fsEx != null)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }
  }

  companion object {
    const val ALICE_EMAIL = "alice_hunter@test.com"
    const val BOB_EMAIL = "bob_hunter@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
  }
}
