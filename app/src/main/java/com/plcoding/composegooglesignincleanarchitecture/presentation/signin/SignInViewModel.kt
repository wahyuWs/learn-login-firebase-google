package com.plcoding.composegooglesignincleanarchitecture.presentation.signin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel: ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        ) }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}

/*
fun onSignInResult(result: SignInResult)
digunakan untuk memperbarui status SignInState berdasarkan hasil dari proses masuk (sign-in). Fungsi ini menerima parameter result yang merupakan objek SignInResult. Pada bagian ini, _state di-update dengan menggunakan fungsi update dari MutableStateFlow. update menerima sebuah lambda yang memperbarui nilai _state dengan meng-copy nilai saat ini dan mengganti beberapa propertinya berdasarkan nilai result

fun resetState()
digunakan untuk mengembalikan nilai _state ke nilai default, yaitu membuat instance baru dari SignInState()
**/