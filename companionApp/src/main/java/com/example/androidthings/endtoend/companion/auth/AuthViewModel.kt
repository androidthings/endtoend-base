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
import com.example.androidthings.endtoend.companion.data.GizmoDao
import com.example.androidthings.endtoend.companion.util.Event
import com.google.firebase.auth.UserInfo

class AuthViewModel(
    private val authProvider: AuthProvider,
    private val gizmoDao: GizmoDao
) : ViewModel(), AuthProvider by authProvider {

    // We don't want clients to be able to set what is stored in our MutableLiveData, so we expose
    // it as a regular LiveData instead.
    private val _authStateModelLiveData = MutableLiveData<Event<AuthStateModel>>()
    val authStateModelLiveData: LiveData<Event<AuthStateModel>>
        get() = _authStateModelLiveData

    private val _authUiModelLiveData = MutableLiveData<AuthUiModel>()
    val authUiModelLiveData: LiveData<AuthUiModel>
        get() = _authUiModelLiveData

    private var userInfo: UserInfo? = null
    private var authUiModel = AuthUiModel(true, false, null) // Initializing

    private val userObserver = Observer<UserInfo?> { user ->
        resolveAuthStateModel(user)
        setAuthUiModel(authUiModel.copy(initializing = false, user = user))
        gizmoDao.setUser(user?.uid)
    }

    init {
        // Show initializing state
        setAuthUiModel(authUiModel)
        // Watch for changes to signed in user
        userLiveData.observeForever(userObserver)
    }

    override fun onCleared() {
        super.onCleared()
        userLiveData.removeObserver(userObserver)
    }

    private fun setAuthUiModel(model: AuthUiModel) {
        authUiModel = model
        _authUiModelLiveData.value = authUiModel
    }

    // Compare new UserInfo with the previous the determine if (and what kind of) change occurred.
    private fun resolveAuthStateModel(newInfo: UserInfo?) {
        val oldUid = userInfo?.uid
        val newUid = newInfo?.uid
        userInfo = newInfo

        if (oldUid != newUid) {
            val stateChange = when {
                oldUid == null -> AuthStateChange.SIGNED_IN
                newUid == null -> AuthStateChange.SIGNED_OUT
                else -> AuthStateChange.USER_CHANGED
            }
            _authStateModelLiveData.value = Event(AuthStateModel(stateChange, newInfo))
        }
    }

    /** Initiates sign in action with the AuthProvider. */
    override fun performSignIn() {
        // TODO maybe skip if we're initializing or already have a user
        setAuthUiModel(authUiModel.copy(authInProgress = true))
        authProvider.performSignIn()
    }

    /** Initiates sign out action with the AuthProvider. */
    override fun performSignOut() {
        setAuthUiModel(authUiModel.copy(authInProgress = true))
        authProvider.performSignOut()
    }

    /** To be called by the AuthProvider with the result of an auth action. */
    fun onAuthResult(result: AuthActionResult) {
        setAuthUiModel(authUiModel.copy(authInProgress = false))
        // TODO maybe show a snackbar if result is FAIL
    }

    enum class AuthActionResult {
        SUCCESS, FAIL, CANCEL
    }

    /** Model describing the current authentication state and the change that resulted in it. */
    data class AuthStateModel(
        val authStateChange: AuthStateChange,
        val user: UserInfo?
    )

    enum class AuthStateChange {
        SIGNED_IN, SIGNED_OUT, USER_CHANGED
    }

    /** Model containing data for showing a UI around authentication state and actions. */
    data class AuthUiModel(
        val initializing: Boolean,
        val authInProgress: Boolean,
        val user: UserInfo?
    )
}
