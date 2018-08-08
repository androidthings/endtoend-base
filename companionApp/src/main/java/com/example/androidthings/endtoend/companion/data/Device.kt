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

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/** A device the user can interact with from the companion app. */
@IgnoreExtraProperties
data class Device(
    // This needs to be nullable because Device instances can (and are) built from documents
    // received from Firestore, and in that case the id is the document ID itself (so it won't be
    // *in* the document). Therefore we need to be able to construct it and then set the ID.
    // Additionally, when adding a Device, leaving this as null lets Firestore generate an ID.
    @Exclude var id: String? = null,
    var name: String = "",
    var type: String = ""
) {
    // Only ID considered for equality
    override fun equals(other: Any?) = other is Device && id == other.id

    // Only ID considered for equality
    override fun hashCode() = id?.hashCode() ?: 0

    fun areContentsTheSame(other: Device): Boolean {
        return id == other.id && name == other.name && type == other.type
    }
}
