package com.example.billboardinsight.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.billboardinsight.model.UserModel
import com.example.billboardinsight.model.UserPreference
import kotlinx.coroutines.launch

class MainViewModel(private val preference: UserPreference) : ViewModel() {

//    fun getUser(): LiveData<UserModel> {
//        return preference.getUser().asLiveData()
//    }

    fun logout() {
        viewModelScope.launch {
            preference.logout()
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}