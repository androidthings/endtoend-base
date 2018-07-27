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
import com.google.firebase.auth.UserInfo

/** Provides authentication state and functionality to the app. */
abstract class AuthProvider {

    // We don't want clients to be able to set what is stored in our MutableLiveData, so we expose
    // it as a regular LiveData instead.
    protected val _userLiveData = MutableLiveData<UserInfo?>()
    val userLiveData: LiveData<UserInfo?>
        get() = _userLiveData

    abstract fun performSignIn()

    abstract fun performSignOut()
}
