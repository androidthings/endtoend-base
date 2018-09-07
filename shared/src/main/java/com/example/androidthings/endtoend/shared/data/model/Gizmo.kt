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

package com.example.androidthings.endtoend.shared.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/** A hardware unit the user can interact with from the companion app. */
@IgnoreExtraProperties
data class Gizmo(
    // Firestore requires a default constructor, so we need properties to be mutable and have
    // defaults in order to let Firestore instantiate and populate them.
    @Exclude var id: String = "",
    var name: String = "",
    var type: String = "",
    var nicknames: List<String> = emptyList(),
    var toggles: List<Toggle> = emptyList()
) {
    val displayName: String
        get() = if (nicknames.isNotEmpty()) nicknames[0] else name

    // Only ID considered for equality
    override fun equals(other: Any?) = (this === other) || (other is Gizmo && id == other.id)

    // Only ID considered for equality
    override fun hashCode() = id.hashCode()
}
