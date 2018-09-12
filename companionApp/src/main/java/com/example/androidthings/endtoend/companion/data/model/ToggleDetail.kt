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

package com.example.androidthings.endtoend.companion.data.model

import com.example.androidthings.endtoend.shared.data.model.Toggle

/**
 * Data model for Gizmo detail screen. Can store additional UI state without polluting the base
 * entity from the shared module.
 */
data class ToggleDetail(
    val toggle: Toggle,
    val progress: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        return other == this || (other is ToggleDetail && other.toggle == this.toggle)
    }

    override fun hashCode() = toggle.hashCode()
}
