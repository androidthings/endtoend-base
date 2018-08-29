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

import android.util.Log
import com.example.androidthings.endtoend.companion.data.ToggleCommand
import com.example.androidthings.endtoend.shared.domain.Result
import com.example.androidthings.endtoend.shared.domain.UseCase
import com.example.androidthings.endtoend.shared.util.jsonArray
import com.example.androidthings.endtoend.shared.util.jsonObject
import com.google.firebase.functions.FirebaseFunctions
import org.json.JSONObject
import java.util.UUID

/** Wrapper class that reports the result state of a given request. */
data class SendToggleCommandResult(
    val command: ToggleCommand,
    val result: Result<Unit>
)

/** Use case that sends a toggle request to a Cloud Function. */
class SendToggleCommandUseCase : UseCase<ToggleCommand, SendToggleCommandResult>() {

    private val firebaseFunctions = FirebaseFunctions.getInstance()

    override fun execute(parameters: ToggleCommand) {
        val json = commandToJsonPayload(parameters)
        if (json == null) {
            result.postValue(
                SendToggleCommandResult(
                    parameters,
                    Result.Error(IllegalArgumentException("Error converting command to JSON"))
                )
            )
            return
        }

        // Notify that we're loading
        result.postValue(
            SendToggleCommandResult(parameters, Result.Loading)
        )

        // Call the cloud function
        firebaseFunctions.getHttpsCallable("toggleCommand") // TODO replace with function name
            .call(json)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result.postValue(
                        SendToggleCommandResult(parameters, Result.success)
                    )
                } else {
                    val ex = task.exception ?: RuntimeException("Task failed with unknown error")
                    result.postValue(
                        SendToggleCommandResult(parameters, Result.Error(ex))
                    )
                }
            }
    }

    /**
     * Convert a [ToggleCommand] to a JSON payload for the Cloud Function. Returns null if the
     * conversion fails.
     */
    private fun commandToJsonPayload(command: ToggleCommand): JSONObject? {
        try {
            return jsonObject(
                "requestId" to UUID.randomUUID().toString(),
                "inputs" to jsonArray(
                    jsonObject(
                        "intent" to "action.devices.EXECUTE",
                        "payload" to jsonObject(
                            "commands" to jsonArray(
                                jsonObject(
                                    "devices" to jsonArray(
                                        jsonObject(
                                            "id" to command.gizmoId,
                                            "customData" to jsonObject(
                                                // Cloud Function needs this to look up FCM token
                                                "userId" to command.userId
                                            )
                                        )
                                    ),
                                    "execution" to jsonArray(
                                        jsonObject(
                                            "command" to "action.devices.commands.SetToggles",
                                            "params" to jsonObject(
                                                "updateToggleSettings" to jsonObject(
                                                    command.toggleId to command.targetState
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("SendToggleCommandUseCase", "Error buillding payload from command: $command")
        }
        return null
    }
}
