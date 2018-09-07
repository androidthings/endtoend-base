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
import com.example.androidthings.endtoend.shared.data.PATH_GIZMOS
import com.example.androidthings.endtoend.shared.data.PATH_USERS
import com.example.androidthings.endtoend.shared.data.asyncExecutor
import com.example.androidthings.endtoend.shared.data.model.Gizmo
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

/** Data source for information about a user's [Gizmo]s. */
interface GizmoDao {
    fun setUser(userId: String?)
    fun getObservableGizmos(): LiveData<List<Gizmo>>
    fun getObservableGizmo(userInfo: UserInfo, gizmoId: String): LiveData<Gizmo?>
}

class FirestoreGizmoDao : GizmoDao {
    private val firestore = FirebaseFirestore.getInstance()

    private var userId: String? = null
    private var listenerRegistration: ListenerRegistration? = null
    private val liveData = MutableLiveData<List<Gizmo>>()

    private val map = mutableMapOf<String, Gizmo>()

    init {
        liveData.postValue(emptyList())
    }

    override fun setUser(userId: String?) {
        if (this.userId == userId) {
            // Already observing this user's Gizmos.
            return
        }
        this.userId = userId

        // Clean up.
        listenerRegistration?.remove()
        listenerRegistration = null
        map.clear()

        if (userId == null) {
            // We have nothing to observe, so present an empty data set.
            liveData.postValue(emptyList())
            return
        }

        liveData.postValue(null) // null while loading, to differentiate from empty

        val query = firestore.collection(PATH_USERS)
            .document("google-oauth2|112379635499756325744") // todo use userId
            .collection(PATH_GIZMOS)
        listenerRegistration = query.addSnapshotListener(
            asyncExecutor,
            EventListener { snapshot, exception ->
                if (snapshot != null) {
                    processSnapshot(snapshot)
                }
            }
        )
    }

    private fun processSnapshot(snapshot: QuerySnapshot) {
        var changed = false
        snapshot.documentChanges.forEach { documentChange ->
            changed = when (documentChange.type) {
                DocumentChange.Type.ADDED,
                DocumentChange.Type.MODIFIED -> addOrUpdateGizmo(documentChange.document)
                DocumentChange.Type.REMOVED -> removeGizmo(documentChange.document.id)
            } or changed
        }
        if (changed) {
            liveData.postValue(map.values.toList())
        }
    }

    /** @return true if the internal data structure has changed. */
    private fun addOrUpdateGizmo(document: DocumentSnapshot): Boolean {
        val gizmo = documentSnapshotToGizmo(document) ?: return false
        map[gizmo.id] = gizmo
        return true
    }

    /** @return true if the internal data structure has changed. */
    private fun removeGizmo(id: String): Boolean {
        return map.remove(id) != null
    }

    private fun documentSnapshotToGizmo(doc: DocumentSnapshot): Gizmo? {
        return if (doc.exists()) {
            doc.toObject(Gizmo::class.java)?.apply { id = doc.id }
        } else null
    }

    override fun getObservableGizmos(): LiveData<List<Gizmo>> = liveData

    override fun getObservableGizmo(userInfo: UserInfo, gizmoId: String): LiveData<Gizmo?> {
        return liveData.map { gizmos ->
            gizmos.find { it.id == gizmoId }
        }
    }
}
