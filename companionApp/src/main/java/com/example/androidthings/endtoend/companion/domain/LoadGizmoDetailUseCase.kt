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

package com.example.androidthings.endtoend.companion.domain

import androidx.lifecycle.LiveData
import com.example.androidthings.endtoend.companion.data.GizmoDao
import com.example.androidthings.endtoend.companion.data.ToggleCommand
import com.example.androidthings.endtoend.companion.data.ToggleCommandDao
import com.example.androidthings.endtoend.companion.data.model.GizmoDetail
import com.example.androidthings.endtoend.companion.data.model.ToggleDetail
import com.example.androidthings.endtoend.shared.data.model.Gizmo
import com.example.androidthings.endtoend.shared.domain.Result
import com.example.androidthings.endtoend.shared.domain.UseCase

/** UseCase that loads details for a Gizmo. This is a continuous UseCase. */
class LoadGizmoDetailUseCase(
    private val gizmoDao: GizmoDao,
    private val toggleCommandDao: ToggleCommandDao
) : UseCase<String, Result<GizmoDetail?>>() {

    private var currentGizmoId: String? = null
    private var gizmoLiveData: LiveData<Gizmo?>? = null
    private var toggleCommandsLiveData: LiveData<List<ToggleCommand>>? = null

    override fun execute(parameters: String) {
        if (parameters == currentGizmoId) {
            return // We're already observing this Gizmo
        }
        currentGizmoId = parameters

        gizmoLiveData?.let {
            result.removeSource(it)
        }
        toggleCommandsLiveData?.let {
            result.removeSource(it)
        }

        result.postValue(Result.Loading)
        try {
            gizmoLiveData = gizmoDao.getObservableGizmo(parameters).also { source ->
                result.addSource(source) { value ->
                    scheduler.execute {
                        resolveAndMerge(value)
                    }
                }
            }
            toggleCommandsLiveData =
                toggleCommandDao.observeToggleCommands(parameters).also { source ->
                    result.addSource(source) {
                        scheduler.execute {
                            merge()
                        }
                    }
                }
        } catch (e: Exception) {
            gizmoLiveData = null
            result.postValue(Result.Error(e))
        }
    }

    private fun resolveAndMerge(gizmo: Gizmo?) {
        if (gizmo == null) {
            // Nothing to do, just post the result.
            result.postValue(Result.Success(null))
            return
        }

        val resolved = toggleCommandDao.resolveToggleCommands(gizmo)
        // If nothing was resolved, we should merge the data now. Otherwise the ToggleCommand
        // LiveData will trigger a merge when it receives updated ToggleCommands.
        if (resolved.isEmpty()) {
            merge()
        }
    }

    private fun merge() {
        val gizmo = gizmoLiveData?.value
        if (gizmo == null) {
            // Nothing to do, just post the result.
            result.postValue(Result.Success(gizmo))
            return
        }

        val commands = toggleCommandsLiveData?.value ?: emptyList()
        val toggleDetails = mutableListOf<ToggleDetail>()
        for (toggle in gizmo.toggles) {
            toggleDetails.add(
                ToggleDetail(toggle, commands.find { it.toggleId == toggle.id } != null)
            )
        }
        result.postValue(Result.Success(GizmoDetail(gizmo, toggleDetails)))
    }
}
