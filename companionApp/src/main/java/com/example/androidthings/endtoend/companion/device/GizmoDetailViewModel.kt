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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.androidthings.endtoend.companion.auth.AuthProvider
import com.example.androidthings.endtoend.companion.data.ToggleCommand
import com.example.androidthings.endtoend.companion.domain.LoadGizmoDetailUseCase
import com.example.androidthings.endtoend.companion.domain.SendToggleCommandParameters
import com.example.androidthings.endtoend.companion.domain.SendToggleCommandUseCase
import com.example.androidthings.endtoend.companion.util.Event
import com.example.androidthings.endtoend.shared.data.model.Toggle
import com.example.androidthings.endtoend.shared.domain.Result

class GizmoDetailViewModel(
    private val authProvider: AuthProvider,
    private val loadGizmoDetailUseCase: LoadGizmoDetailUseCase,
    private val sendToggleCommandUseCase: SendToggleCommandUseCase
) : ViewModel() {

    // We can't load until we have a gizmo ID, so use this to check.
    private var gizmoId: String? = null

    val gizmoLiveData = loadGizmoDetailUseCase.observe()

    // Used to show error events in the UI.
    private val sendToggleCommandErrorLiveDataInternal = MediatorLiveData<Event<ToggleCommand>>()
    val sendToggleCommandErrorLiveData: LiveData<Event<ToggleCommand>>
        get() = sendToggleCommandErrorLiveDataInternal

    init {
        sendToggleCommandErrorLiveDataInternal.addSource(sendToggleCommandUseCase.observe()) {
            if (it.result is Result.Error) {
                sendToggleCommandErrorLiveDataInternal.postValue(Event(it.command))
            }
        }
    }

    fun setGizmoId(gizmoId: String) {
        if (this.gizmoId != gizmoId) {
            this.gizmoId = gizmoId
            loadGizmoDetailUseCase.execute(gizmoId)
        }
    }

    fun onToggleClicked(toggle: Toggle) {
        val gizmoId = gizmoId ?: return
        val user = authProvider.userLiveData.value ?: return
        // Send toggle command
        sendToggleCommandUseCase.execute(
            SendToggleCommandParameters(
                user.uid,
                ToggleCommand(gizmoId, toggle.id, !toggle.on),
                TOGGLE_COMMAND_TIMEOUT
            )
        )
    }
}

private const val TOGGLE_COMMAND_TIMEOUT = 1000L * 10 // 10 seconds
