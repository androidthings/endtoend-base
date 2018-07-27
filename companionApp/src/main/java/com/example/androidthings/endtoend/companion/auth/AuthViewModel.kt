/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.endtoend.companion.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.UserInfo

class AuthViewModel(private val authProvider: AuthProvider): ViewModel() {

    private var authState = AuthState(true, false, null) // Initializing state

    // We don't want clients to be able to set what is stored in our MutableLiveData, so we expose
    // it as a regular LiveData instead.
    private val _authStateLiveData = MutableLiveData<AuthState>()
    val authStateLiveData: LiveData<AuthState>
        get() = _authStateLiveData

    private val userObserver = Observer<UserInfo?> { user ->
        setAuthState(authState.copy(initializing = false, user = user))
    }

    init {
        // Show initializing state
        setAuthState(authState)
        // Watch for changes to signed in user
        authProvider.userLiveData.observeForever(userObserver)
    }

    override fun onCleared() {
        super.onCleared()
        authProvider.userLiveData.removeObserver(userObserver)
    }

    private fun setAuthState(state: AuthState) {
        authState = state
        _authStateLiveData.value = authState
    }

    fun signIn() {
        // TODO maybe skip if we're initializing or already have a user
        setAuthState(authState.copy(authInProgress = true))
        authProvider.performSignIn()
    }

    fun signOut() {
        setAuthState(authState.copy(authInProgress = true))
        authProvider.performSignOut()
    }

    fun onAuthResult(result: AuthResult) {
        setAuthState(authState.copy(authInProgress = false))
        // TODO maybe show a snackbar if result is FAIL
    }

    data class AuthState(
        val initializing: Boolean,
        val authInProgress: Boolean,
        val user: UserInfo?
    )

    enum class AuthResult {
        SUCCESS, FAIL, CANCEL
    }
}
