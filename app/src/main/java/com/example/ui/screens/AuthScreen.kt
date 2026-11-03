package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.CyberCard
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var isRegisterMode by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var hunterName by remember { mutableStateOf("") }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var infoMessage by remember { mutableStateOf<String?>(null) }
  var showResetDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground)
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Emblem / Logo
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(CircleShape)
          .background(Color(0xFFECFDF5))
          .border(2.dp, CleanPrimary, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "🌱", fontSize = 36.sp)
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "REBORN",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        letterSpacing = 1.sp
      )
      Text(
        text = "REBUILD YOURSELF. LEVEL UP YOUR LIFE.",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = CleanPrimary,
        letterSpacing = 0.5.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Error / Info banner
      AnimatedVisibility(visible = errorMessage != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFEF2F2),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = errorMessage ?: "", color = Color(0xFFDC2626), fontSize = 12.sp)
          }
        }
      }

      AnimatedVisibility(visible = infoMessage != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFECFDF5),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CleanPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = infoMessage ?: "", color = CleanPrimary, fontSize = 12.sp)
          }
        }
      }

      CyberCard(
        borderColor = CleanBorder
      ) {
        Text(
          text = if (isRegisterMode) "CREATE YOUR ACCOUNT" else "WELCOME BACK",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isRegisterMode) {
          OutlinedTextField(
            value = hunterName,
            onValueChange = { hunterName = it },
            label = { Text("Your Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CleanPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CleanPrimary) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CleanPrimary) },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        if (isRegisterMode) {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CleanPrimary) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }

        if (!isRegisterMode) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Forgot password?",
            fontSize = 12.sp,
            color = CleanPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
              .align(Alignment.End)
              .clickable { showResetDialog = true }
              .padding(vertical = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            errorMessage = null
            infoMessage = null
            if (email.isBlank() || password.isBlank()) {
              errorMessage = "Email and password are required."
              return@Button
            }
            if (isRegisterMode && password != confirmPassword) {
              errorMessage = "Passwords do not match."
              return@Button
            }
            if (password.length < 6) {
              errorMessage = "Password must be at least 6 characters."
              return@Button
            }

            isLoading = true
            coroutineScope.launch {
              try {
                if (isRegisterMode) {
                  Firebase.auth.createUserWithEmailAndPassword(email.trim(), password).await()
                } else {
                  Firebase.auth.signInWithEmailAndPassword(email.trim(), password).await()
                }
                isLoading = false
                onAuthSuccess()
              } catch (e: Exception) {
                isLoading = false
                errorMessage = e.localizedMessage ?: "Authentication failed."
              }
            }
          },
          enabled = !isLoading,
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary, contentColor = Color.White),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
          } else {
            Text(
              text = if (isRegisterMode) "Create Account" else "Sign In",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Divider
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          HorizontalDivider(modifier = Modifier.weight(1f), color = CleanBorder)
          Text(text = " OR ", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
          HorizontalDivider(modifier = Modifier.weight(1f), color = CleanBorder)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Google Sign-In with Credential Manager
        Button(
          onClick = {
            errorMessage = null
            infoMessage = null
            isLoading = true
            onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onAuthError = { err ->
                isLoading = false
                errorMessage = err
              },
              scope = coroutineScope,
              onAuthCancelled = {
                isLoading = false
              }
            )
          },
          enabled = !isLoading,
          colors = ButtonDefaults.buttonColors(containerColor = CleanCardVariant, contentColor = TextPrimary),
          border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "G", fontWeight = FontWeight.Black, fontSize = 16.sp, color = CleanPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Sign in with Google", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
          onClick = {
            isRegisterMode = !isRegisterMode
            errorMessage = null
            infoMessage = null
          },
          modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
          Text(
            text = if (isRegisterMode) "Already have an account? Sign In" else "New here? Create an Account",
            fontSize = 12.sp,
            color = CleanPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }

  // Password Reset Dialog
  if (showResetDialog) {
    var resetEmail by remember { mutableStateOf(email) }
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Reset Password", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Enter your email address to receive password reset instructions.", color = TextSecondary, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = resetEmail,
            onValueChange = { resetEmail = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (resetEmail.isNotBlank()) {
              Firebase.auth.sendPasswordResetEmail(resetEmail.trim())
              infoMessage = "Password reset instructions sent to $resetEmail"
              showResetDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary, contentColor = Color.White)
        ) {
          Text("Send Reset Link")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = CleanCard
    )
  }
}

fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope,
  onAuthCancelled: () -> Unit = {}
) {
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
    return
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(context as Activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onAuthError("Unexpected credential type")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      onAuthCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onAuthError(e.localizedMessage ?: "Sign in failed")
    }
  }
}
