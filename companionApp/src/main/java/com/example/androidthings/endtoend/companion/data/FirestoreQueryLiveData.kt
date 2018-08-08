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

import com.example.androidthings.endtoend.companion.util.DebouncedDeactivateLiveData
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.concurrent.Executor

/**
 * LiveData implementation that watches a Firestore [Query] as long as there are any active
 * observers.
 */
class FirestoreQueryLiveData<T>(
    private val query: Query,
    private val snapshotProcessor: (QuerySnapshot) -> T,
    private val executor: Executor
) : DebouncedDeactivateLiveData<T>() {
    private lateinit var listener: ListenerRegistration

    override fun onActivate() {
        super.onActivate()
        listener = query.addSnapshotListener(
            executor,
            EventListener<QuerySnapshot> { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null) {
                    val result = snapshotProcessor(querySnapshot)
                    postValue(result)
                }
            })
    }

    override fun onDeactivate() {
        super.onDeactivate()
        @Suppress("UNNECESSARY_SAFE_CALL")
        listener?.remove()
    }
}
