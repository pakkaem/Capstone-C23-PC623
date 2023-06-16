package com.example.billboardinsight.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billboardinsight.model.UserModel
import com.example.billboardinsight.model.UserPreference
import com.example.billboardinsight.remote.ApiConfig
import com.example.billboardinsight.response.AuthResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _emailValid = MutableLiveData<Boolean>()
    private val _passwordValid = MutableLiveData<Boolean>()
    private val _loading = MutableLiveData<Boolean>()

    val emailValid: LiveData<Boolean> = _emailValid
    val passwordValid: LiveData<Boolean> = _passwordValid
    val statusMessage = MutableLiveData<String>()
    val loading: LiveData<Boolean> = _loading

    init {
        _emailValid.value = false
        _passwordValid.value = false
        statusMessage.value = ""
        _loading.value = false
    }

    fun updateEmailStatus(status: Boolean) {
        _emailValid.value = status
    }

    fun updatePasswordStatus(status: Boolean) {
        _passwordValid.value = status
    }

    fun registerUser(name: String, email: String, password: String) {
        _loading.value = true

        val client = ApiConfig.getApiService().register(name, email, password)
        client.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(
                call: Call<AuthResponse>,
                response: Response<AuthResponse>
            ) {
                _loading.value = false

                if (response.isSuccessful) {
                    val result = response.body()

                    if (result != null) {
                        if (!result.error) {
                            statusMessage.value = "success"
                        }
                    }
                } else {
                    // Program will enter this block of code if the email isn't valid by Dicoding backend system.
                    // For example 'ricky@gmail.c' is true when using kotlin email checking but false by backend.
                    // And the email might be already registered
                    Log.e(TAG, response.message())
                    statusMessage.value = response.message()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e(TAG, "Error message: ${t.message}")
                _loading.value = false
                statusMessage.value = t.message
            }
        })
    }

    companion object {
        private const val TAG = "RegisterViewModel"
    }
}