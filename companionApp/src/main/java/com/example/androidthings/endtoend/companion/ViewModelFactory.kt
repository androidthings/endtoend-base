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

package com.example.androidthings.endtoend.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidthings.endtoend.companion.auth.AuthProvider
import com.example.androidthings.endtoend.companion.auth.AuthViewModel
import com.example.androidthings.endtoend.companion.auth.FirebaseAuthProvider
import com.example.androidthings.endtoend.companion.data.FirestoreGizmoDao
import com.example.androidthings.endtoend.companion.data.GizmoDao
import com.example.androidthings.endtoend.companion.device.GizmoDetailViewModel
import com.example.androidthings.endtoend.companion.device.GizmoListViewModel

/**
 * Factory that constructs ViewModel classes throughout the app.
 * TODO Use Dagger2 with @Binds @IntoMap instead
 */
class ViewModelFactory private constructor(
    private val authProvider: AuthProvider,
    private val gizmoDao: GizmoDao
) : ViewModelProvider.Factory {

    companion object {
        val instance = ViewModelFactory(FirebaseAuthProvider, FirestoreGizmoDao())
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            AuthViewModel::class.java -> AuthViewModel(authProvider) as T
            GizmoListViewModel::class.java -> GizmoListViewModel(gizmoDao, authProvider) as T
            GizmoDetailViewModel::class.java -> GizmoDetailViewModel(gizmoDao, authProvider) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
