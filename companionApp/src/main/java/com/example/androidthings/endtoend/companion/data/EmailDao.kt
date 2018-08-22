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

import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

interface EmailDao {
    fun writeUserEmailToUidMapping(user: UserInfo)
}

class FirebaseEmailDao : EmailDao {
    private val firestore = FirebaseFirestore.getInstance()

    override fun writeUserEmailToUidMapping(user: UserInfo) {
        user.email?.let { email ->
            val collection = firestore.collection(PATH_EMAILS)
            val doc = collection.document(email)
            doc.get().addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                }
                val snapshot = it.result
                if (!snapshot.exists()) {
                    // Add a new doc with this field
                    doc.set(mapOf(FIELD_FIREBASE_ID to user.uid))
                } else if (!hasMatchingFirebaseId(user, snapshot)) {
                    // Update the field in the doc
                    doc.update(FIELD_FIREBASE_ID, user.uid)
                }
            }
        }
    }

    private fun hasMatchingFirebaseId(user: UserInfo, snapshot: DocumentSnapshot): Boolean {
        return (snapshot.get(FIELD_FIREBASE_ID) as? String) == user.uid
    }
}

