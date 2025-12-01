package com.example.miniyam.Presentation.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.Data.api.AuthService
import com.example.miniyam.Data.model.Auth.LoginRequest
import com.example.miniyam.Data.model.Auth.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class RegStates{
    NONE, USEREXISTS, WRONGPASSWORD, OK, SERVERERROR, USERNOTEXIST
}


class RegAuthViewModel(application: Application): AndroidViewModel(application){

    private val _name= MutableStateFlow<String>("")
    val name:StateFlow<String> =_name
    private val _email= MutableStateFlow<String>("")
    val email:StateFlow<String> =_email
    private val _password= MutableStateFlow<String>("")
    val password:StateFlow<String> =_password

    private val _successAuth=MutableStateFlow<RegStates>(RegStates.NONE)
    val successAuth:StateFlow<RegStates> =_successAuth
    private val _isReg= MutableStateFlow<Boolean>(true)
    val isReg:StateFlow<Boolean> =_isReg

    @SuppressLint("StaticFieldLeak")
    val context = application.applicationContext

    fun changeRegState() {
        _isReg.value=!_isReg.value
    }

    fun onSubmitName(name: String){
        _name.value=name
    }
    fun onSubmitEmail(email: String){
        _email.value=email
    }
    fun onSubmitPassword(password: String) {
        _password.value = password
    }

    fun auth() {
        val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        if (isReg.value){
             register(sharedPref)
        }
        else{
            login(sharedPref)
        }

    }

    fun register(sharedPref: SharedPreferences) {
        viewModelScope.launch {
            try {
                val result = AuthService.api.register(
                    RegisterRequest(
                        name = name.value,
                        email = email.value,
                        password = password.value
                    )
                )
                sharedPref.edit()
                    .putString("token", result.message)
                    .apply()
                _successAuth.value = RegStates.OK
            } catch (e: HttpException) {
                when (e.code()) {
                    409 -> _successAuth.value = RegStates.USEREXISTS
                    else -> _successAuth.value = RegStates.SERVERERROR
                }
            } catch (e: Exception) {
                println("Exception: $e")
                _successAuth.value = RegStates.SERVERERROR
            }
        }
    }

    fun login(sharedPref: SharedPreferences) {
        viewModelScope.launch {
            try {
                val response = AuthService.api.login(LoginRequest(_email.value, _password.value))
                _successAuth.value = RegStates.OK
                val token = try {
                    response.message
                } catch (_: Exception) {
                    null
                }
                if (!token.isNullOrBlank()) {
                    sharedPref.edit().putString("token", token).apply()
                }
            } catch (e: HttpException) {
                _successAuth.value = when (e.code()) {
                    401 -> RegStates.WRONGPASSWORD
                    404 -> RegStates.USERNOTEXIST
                    else -> RegStates.SERVERERROR
                }
            } catch (e: Exception) {
                _successAuth.value = RegStates.SERVERERROR
            }
        }
    }

}






