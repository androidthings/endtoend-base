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
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

/** Data source for information about a user's [Gizmo]s. */
interface GizmoDao {
    fun getObservableGizmos(userInfo: UserInfo): LiveData<List<Gizmo>>
    fun getObservableGizmo(userInfo: UserInfo, gizmoId: String): LiveData<Gizmo?>
}

class FirestoreGizmoDao : GizmoDao {
    private val firestore = FirebaseFirestore.getInstance()

    override fun getObservableGizmos(userInfo: UserInfo): LiveData<List<Gizmo>> {
        val query = firestore.collection(PATH_USERS)
            .document("google-oauth2|112379635499756325744") // todo use uid from userInfo
            .collection(PATH_GIZMOS)
        return FirestoreLiveData.forQuery(query, ::querySnapshotToGizmos, asyncExecutor)
    }

    override fun getObservableGizmo(userInfo: UserInfo, gizmoId: String): LiveData<Gizmo?> {
        val query = firestore.collection(PATH_USERS)
            .document("google-oauth2|112379635499756325744") // todo use uid from userInfo
            .collection(PATH_GIZMOS)
            .document(gizmoId)
        return FirestoreLiveData.forDocument(query, ::documentSnapshotToGizmo, asyncExecutor)
    }

    private fun querySnapshotToGizmos(snapshot: QuerySnapshot): List<Gizmo> {
        return snapshot.documents.mapNotNull { documentSnapshotToGizmo(it) }
    }

    private fun documentSnapshotToGizmo(doc: DocumentSnapshot): Gizmo? {
        return if (doc.exists()) {
            doc.toObject(Gizmo::class.java)?.apply { id = doc.id }
        } else null
    }
}
