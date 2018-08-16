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

import com.example.androidthings.endtoend.companion.data.FirestoreLiveData.Companion.forDocument
import com.example.androidthings.endtoend.companion.data.FirestoreLiveData.Companion.forQuery
import com.example.androidthings.endtoend.companion.util.DebouncedDeactivateLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.concurrent.Executor

/**
 * LiveData implementation that watches Firestore data as long as there are any active observers.
 * Obtain one using [forQuery] or [forDocument] depending on what kind of data will be watched.
 *
 * @param T the type this LiveData stores
 * @param SNAPSHOT the type of snapshot from Firestore
 * @property snapshotProcessor function that converts a snapshot into the desired result type
 * @property executor Executor on which the snapshotProcessor will be invoked
 */
abstract class FirestoreLiveData<T, SNAPSHOT>(
    protected val snapshotProcessor: (SNAPSHOT) -> T,
    protected val executor: Executor
) : DebouncedDeactivateLiveData<T>() {

    companion object {

        /**
         * Get a LiveData that watches the given Firestore Query.
         *
         * @param query the Query to watch
         * @param snapshotProcessor function that converts a snapshot into the desired result type
         * @param executor Executor on which the snapshotProcessor will be invoked
         */
        fun <T> forQuery(
            query: Query,
            snapshotProcessor: (QuerySnapshot) -> T,
            executor: Executor
        ): FirestoreLiveData<T, QuerySnapshot> =
            FirestoreQueryLiveData(query, snapshotProcessor, executor)

        /**
         * Get a LiveData that watches the given Firestore Document.
         *
         * @param document the Document to watch
         * @param snapshotProcessor function that converts a snapshot into the desired result type
         * @param executor Executor on which the snapshotProcessor will be invoked
         */
        fun <T> forDocument(
            document: DocumentReference,
            snapshotProcessor: (DocumentSnapshot) -> T,
            executor: Executor
        ): FirestoreLiveData<T, DocumentSnapshot> =
            FirestoreDocumentLiveData(document, snapshotProcessor, executor)
    }

    private lateinit var listener: ListenerRegistration

    override fun onActivate() {
        super.onActivate()
        listener = registerListener(
            executor,
            EventListener { snapshot, firebaseFirestoreException ->
                if (snapshot != null) {
                    val result = snapshotProcessor(snapshot)
                    postValue(result)
                }
            })
    }

    override fun onDeactivate() {
        super.onDeactivate()
        @Suppress("UNNECESSARY_SAFE_CALL")
        listener?.remove()
    }

    /** Implement adding the snapshot listener to the Firestore object. */
    abstract fun registerListener(
        executor: Executor,
        listener: EventListener<SNAPSHOT>
    ): ListenerRegistration
}

/** LiveData that watches a Firestore Query. */
private class FirestoreQueryLiveData<T>(
    private val query: Query,
    snapshotProcessor: (QuerySnapshot) -> T,
    executor: Executor
) : FirestoreLiveData<T, QuerySnapshot>(snapshotProcessor, executor) {

    override fun registerListener(
        executor: Executor,
        listener: EventListener<QuerySnapshot>
    ): ListenerRegistration = query.addSnapshotListener(executor, listener)
}

/** LiveData that watches a Firestore Document. */
private class FirestoreDocumentLiveData<T>(
    private val document: DocumentReference,
    snapshotProcessor: (DocumentSnapshot) -> T,
    executor: Executor
) : FirestoreLiveData<T, DocumentSnapshot>(snapshotProcessor, executor) {

    override fun registerListener(
        executor: Executor,
        listener: EventListener<DocumentSnapshot>
    ): ListenerRegistration = document.addSnapshotListener(executor, listener)
}
