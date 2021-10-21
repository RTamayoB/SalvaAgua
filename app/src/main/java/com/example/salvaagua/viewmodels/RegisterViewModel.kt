package com.example.salvaagua.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.salvaagua.R
import com.example.salvaagua.data.repositories.RegisterRepository
import com.example.salvaagua.ui.register.RegisterFormState

class RegisterViewModel(private val registerRepository: RegisterRepository): ViewModel() {

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    fun register(name: String, lastName: String, email: String, password: String, confirmPassword: String){
        registerRepository.register()
    }

    fun registerDataChanged(
        name: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String){
        if(!isNameValid(name)){
            _registerForm.value = RegisterFormState(nameError = R.string.invalid_name)
        }
        else if(!isLastNameValid(lastName)){
            _registerForm.value = RegisterFormState(lastNameError = R.string.invalid_lastname)
        }
        else if(!isEmailValid(email)){
            _registerForm.value = RegisterFormState(emailError = R.string.invalid_email)
        }
        else if(!isPasswordValid(password)){
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_pass)
        }
        else if(!isPasswordConfirmValid(password, confirmPassword)){
            _registerForm.value = RegisterFormState(confirmPasswordError = R.string.invalid_confirm_password)
        }
        else{
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    private fun isNameValid(name: String): kotlin.Boolean{
        return name.isNotBlank()
    }

    private fun isLastNameValid(lastName: String): Boolean{
        return lastName.isNotBlank()
    }

    private fun isEmailValid(email: String): Boolean {
        return if (email.contains("@")) {
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isNotBlank()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isPasswordConfirmValid(password: kotlin.String, confirmPassword: kotlin.String): Boolean {
        return confirmPassword.equals(password)
    }

}