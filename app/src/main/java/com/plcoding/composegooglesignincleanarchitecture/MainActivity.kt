package com.plcoding.composegooglesignincleanarchitecture

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.plcoding.composegooglesignincleanarchitecture.presentation.profile.ProfileScreen
import com.plcoding.composegooglesignincleanarchitecture.presentation.signin.GoogleAuthClient
import com.plcoding.composegooglesignincleanarchitecture.presentation.signin.SignInScreen
import com.plcoding.composegooglesignincleanarchitecture.presentation.signin.SignInViewModel
import com.plcoding.composegooglesignincleanarchitecture.ui.theme.ComposeGoogleSignInCleanArchitectureTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val googleAuthClient by lazy {
        GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeGoogleSignInCleanArchitectureTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {
                        composable("sign_in") {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            //jika sudah login, navigasi ke profile
                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )
                            
                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(applicationContext, "Sign In successfull", Toast.LENGTH_LONG).show()

                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }
                            
                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signIntentSender = googleAuthClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                userData = googleAuthClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        //logout account
                                        googleAuthClient.signOut()
                                        Toast.makeText(applicationContext, "Signed Out", Toast.LENGTH_LONG).show()

                                        navController.popBackStack() //kembali ke navigasi sebelumnya
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
private val googleAuthClient by lazy
lazy: Ini adalah cara untuk menunda inisialisasi properti hingga pertama kali diakses. Dengan menggunakan lazy, objek GoogleAuthClient tidak akan dibuat sampai properti ini benar-benar diminta.

val state by viewModel.state.collectAsStateWithLifecycle()
collectAsStateWithLifecycle untuk mengamati dan mengambil status dari state di dalam ViewModel. Jadi, state akan selalu mengikuti perubahan status di dalam ViewModel.

val launcher = rememberLauncherForActivityResult
Kode ini menggunakan rememberLauncherForActivityResult untuk membuat objek launcher yang akan digunakan untuk memulai suatu aktivitas dan menangani hasilnya. Dalam kasus ini, StartIntentSenderForResult digunakan sebagai kontrak untuk memulai suatu IntentSender dan menangani hasilnya.
Ketika hasil kembali (onResult) dari aktivitas dimiliki, kode memeriksa apakah hasilnya adalah RESULT_OK. Jika iya, maka kode melanjutkan dengan melakukan operasi lain. Di sini, sebuah lifecycleScope.launch digunakan untuk menjalankan operasi tersebut secara asinkron
Dalam blok launch, googleAuthClient.signInWithIntent dipanggil dengan parameter intent yang berasal dari data hasil. Hasil dari panggilan ini kemudian diteruskan ke ViewModel


**/