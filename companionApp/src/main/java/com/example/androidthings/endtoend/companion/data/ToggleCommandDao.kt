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

package com.example.androidthings.endtoend.companion.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.androidthings.endtoend.companion.util.map
import com.example.androidthings.endtoend.shared.data.model.Gizmo

/**
 * Local storage for commands to change the state of a [Toggle]. Each command represents an
 * outstanding request. Clients should remove the request when it is no longer applicable, such as
 * if the client wants to enforce a timeout for commands.
 *
 * Commands can also be removed by resolving them against a [Gizmo]; in that case, a command is
 * removed if the corresponding Toggle reached the target state after the command was issued.
 */
interface ToggleCommandDao {

    /**
     * Store a ToggleCommand.
     *
     * @param command the ToggleCommand
     * @return true if the command was added. This can return false if an existing command for the
     * same Toggle is already present.
     */
    fun addCommand(command: ToggleCommand): Boolean

    /**
     * Remove a ToggleCommand from storage.
     *
     * @param command the ToggleCommand
     * @return true if the command was removed. This can return false if no command for the Toggle
     * was present.
     */
    fun removeCommand(command: ToggleCommand): Boolean

    /**
     * Resolves ToggleCommands for the given Gizmo data. Resolved means the Toggle reached the
     * target state at some time after the ToggleCommand was issued.
     *
     * @param gizmo the Gizmo to inspect
     * @return the list of ToggleCommands that were resolved (and removed from storage)
     */
    fun resolveToggleCommands(gizmo: Gizmo): List<ToggleCommand>

    /**
     * Observe ToggleCommands for a Gizmo.
     *
     * @param gizmoId the ID of the Gizmo
     * @return a LiveData containing ToggleCommands for the Gizmo
     */
    fun observeToggleCommands(gizmoId: String): LiveData<List<ToggleCommand>>
}

class ToggleCommandDaoImpl : ToggleCommandDao {

    private val map = mutableMapOf<String, ToggleCommand>()
    /**
     * LiveData used to make the map observable, to update LiveDatas created by
     * [observeToggleCommands].
     */
    private val liveData = MutableLiveData<Map<String, ToggleCommand>>()

    override fun addCommand(command: ToggleCommand): Boolean {
        // TODO we should use putIfAbsent for thread-safety, but that requires minSdk 24
        if (!map.containsKey(command.requestKey)) {
            map[command.requestKey] = command
            publish()
            return true
        }
        return false
    }

    override fun removeCommand(command: ToggleCommand): Boolean {
        val removed = map.remove(command.requestKey) != null
        if (removed) {
            publish()
        }
        return removed
    }

    override fun resolveToggleCommands(gizmo: Gizmo): List<ToggleCommand> {
        val resolved = mutableListOf<ToggleCommand>()
        for (toggle in gizmo.toggles) {
            val key = ToggleCommand.createKey(gizmo.id, toggle.id)
            val command = map[key] ?: continue

            // Toggle reached target state after the command was issued
            if (toggle.on == command.targetState && toggle.lastUpdated > command.requestTime) {
                map.remove(key)
                resolved.add(command)
            }
        }
        if (resolved.isNotEmpty()) {
            publish()
        }
        return resolved
    }

    override fun observeToggleCommands(gizmoId: String): LiveData<List<ToggleCommand>> {
        return liveData.map { map ->
            map.values.filter { command ->
                command.gizmoId == gizmoId
            }
        }
    }

    private fun publish() {
        liveData.postValue(map)
    }
}
