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

package com.example.androidthings.endtoend.companion.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidthings.endtoend.companion.auth.AuthProvider
import com.example.androidthings.endtoend.companion.domain.LoadUserGizmosUseCase
import com.example.androidthings.endtoend.companion.util.Event
import com.example.androidthings.endtoend.shared.data.model.Gizmo
import com.example.androidthings.endtoend.shared.domain.Result
import com.example.androidthings.endtoend.shared.domain.execute

class GizmoListViewModel(
    private val authProvider: AuthProvider,
    private val loadUserGizmosUseCase: LoadUserGizmosUseCase
) : ViewModel(), AuthProvider by authProvider {

    val gizmoListLiveData: LiveData<Result<List<Gizmo>>> = loadUserGizmosUseCase.observe()

    private val selectedGizmoLiveDataInternal = MutableLiveData<Event<Gizmo>>()
    val selectedGizmoLiveData: LiveData<Event<Gizmo>>
        get() = selectedGizmoLiveDataInternal

    init {
        // Load gizmos.
        loadUserGizmosUseCase.execute()
    }

    fun selectGizmo(gizmo: Gizmo) {
        selectedGizmoLiveDataInternal.postValue(Event(gizmo))
    }
}
