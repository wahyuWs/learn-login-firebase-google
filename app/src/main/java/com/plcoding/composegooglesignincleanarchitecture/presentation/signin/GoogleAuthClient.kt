package com.plcoding.composegooglesignincleanarchitecture.presentation.signin

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.plcoding.composegooglesignincleanarchitecture.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.result?.pendingIntent?.intentSender
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
}

/*
    private val oneTapClient: SignInClient
    objek SignInClient untuk berinteraksi dengan Google One Tap API

    suspend fun signIn(): IntentSender?
    fungsi yang digunakan untuk memulai proses otentikasi. Ini menggunakan oneTapClient.beginSignIn untuk memulai proses otentikasi One Tap. buildSignInRequest() digunakan untuk membangun permintaan otentikasi.

    private fun buildSignInRequest(): BeginSignInRequest
    digunakan untuk membuat permintaan otentikasi. Ini menggunakan GoogleIdTokenRequestOptions untuk mengonfigurasi opsi otentikasi, seperti penggunaan Server Client ID

    suspend fun signInWithIntent(intent: Intent): SignInResult
    mengelola proses otentikasi setelah pengguna memilih akun mereka. Ini mengambil kredensial dari intent yang diterima, menggunakan Google ID Token untuk membuat kredensial Firebase, dan kemudian mencoba masuk dengan kredensial tersebut. Hasilnya dikemas dalam objek SignInResult

    fun getSignedInUser(): UserData?
    digunakan untuk mendapatkan informasi pengguna yang sudah masuk. Ini mengambil informasi dari objek pengguna Firebase dan mengembalikannya sebagai objek UserData

    suspend functions untuk menghindari blocking UI thread.
**/