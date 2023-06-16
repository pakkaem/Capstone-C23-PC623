package com.example.billboardinsight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.billboardinsight.model.UserPreference
import com.example.billboardinsight.ui.login.LoginViewModel
import com.example.billboardinsight.ui.main.MainViewModel
import com.example.billboardinsight.ui.register.RegisterViewModel

class ViewModelFactory(private val pref: UserPreference) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(pref) as T
                modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(pref) as T
                else -> throw IllegalArgumentException("Unknown ViewModel: " + modelClass.name)
            }
        }

        companion object {
            @Volatile
            private var INSTANCE: ViewModelFactory? = null

            @JvmStatic
            fun getInstance(pref: UserPreference): ViewModelFactory {
                if (INSTANCE == null) {
                    synchronized(ViewModelFactory::class.java) {
                        INSTANCE = ViewModelFactory(pref)
                    }
                }
                return INSTANCE as ViewModelFactory
            }
        }
}