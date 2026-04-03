// com/iptvplayer/app/ui/viewmodel/LoginViewModel.kt
package com.iptvplayer.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.PrefsManager
import com.iptvplayer.app.data.model.AuthResponse
import com.iptvplayer.app.data.model.LoginCredentials
import com.iptvplayer.app.data.model.Resource
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = XtreamRepository()
    val prefsManager = PrefsManager(application)

    private val _authState = MutableLiveData<Resource<AuthResponse>>()
    val authState: LiveData<Resource<AuthResponse>> = _authState

    fun login(host: String, username: String, password: String, remember: Boolean) {
        val trimmedHost = host.trim().let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) "http://$it" else it
        }

        if (trimmedHost.isEmpty() || username.isEmpty() || password.isEmpty()) {
            _authState.value = Resource.Error("Please fill in all fields.")
            return
        }

        val credentials = LoginCredentials(trimmedHost, username.trim(), password.trim())

        _authState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.authenticate(credentials)
            if (result is Resource.Success) {
                val userInfo = result.data.userInfo
                if (userInfo.auth == 0) {
                    _authState.value = Resource.Error("Invalid credentials. Please try again.")
                    return@launch
                }
                if (remember) {
                    prefsManager.saveCredentials(credentials)
                }
                _authState.value = result
            } else {
                _authState.value = result
            }
        }
    }

    fun getSavedCredentials() = prefsManager.getCredentials()
    fun isDisclaimerAccepted() = prefsManager.isDisclaimerAccepted()
}
