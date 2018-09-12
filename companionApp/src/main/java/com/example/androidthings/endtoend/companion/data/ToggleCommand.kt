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

import com.google.firebase.Timestamp

/** A user-initiated command to toggle an LED. */
data class ToggleCommand(
    val gizmoId: String,
    val toggleId: String,
    val targetState: Boolean,
    val requestTime: Timestamp = Timestamp.now()
) {
    val requestKey = createKey(gizmoId, toggleId)

    // only requestKey considered for equality
    override fun equals(other: Any?): Boolean {
        return other === this || (other is ToggleCommand && other.requestKey == requestKey)
    }

    // only requestKey considered for equality
    override fun hashCode(): Int = requestKey.hashCode()

    companion object {
        fun createKey(gizmoId: String, toggleId: String): String {
            return "${gizmoId}_${toggleId}"
        }
    }
}
