package com.sumedh.twinmind.app.presentation.ui.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onLoginError: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Observe the signInSuccess state
    LaunchedEffect(uiState.signInSuccess) {
        if (uiState.signInSuccess) {
            onLoginSuccess()
        } else {
            onLoginError()
        }
    }

    // Handle showing error messages
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scaffoldState.snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown() // Reset error state after showing
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                viewModel.onSignInResult(credential)
            } catch (e: ApiException) {

                Log.d("API exception",""+e.localizedMessage)
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) { padding ->
        LoginContent(
            modifier = Modifier.padding(padding),
            isLoading = uiState.isLoading,
            onContinueWithGoogleClick = {
                val gso = getGoogleSignInOptions(context)
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        )
    }
}

private fun getGoogleSignInOptions(context: Context): GoogleSignInOptions {
    // You need to get the web client ID from your google-services.json file
    // or from the Firebase console.
    val webClientId = "945789957603-rvh0dbr114bpm0ctndaa02uuhpf4virf.apps.googleusercontent.com" // <-- IMPORTANT: REPLACE THIS
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        // THIS IS THE CRITICAL LINE THAT WAS MISSING
        .requestScopes(Scope(CalendarScopes.CALENDAR_READONLY))
        .build()
}