package com.plcoding.composegooglesignincleanarchitecture.presentation.signin

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
