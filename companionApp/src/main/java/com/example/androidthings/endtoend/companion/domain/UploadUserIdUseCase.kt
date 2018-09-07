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

import com.example.androidthings.endtoend.shared.data.FIELD_FIREBASE_ID
import com.example.androidthings.endtoend.shared.data.PATH_EMAILS
import com.example.androidthings.endtoend.shared.domain.Result
import com.example.androidthings.endtoend.shared.domain.UseCase
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

data class UploadUserIdResult(
    val userInfo: UserInfo,
    val result: Result<Unit>
)

class UploadUserIdUseCase : UseCase<UserInfo, UploadUserIdResult>() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun execute(parameters: UserInfo) {
        val email = parameters.email
        if (email == null) {
            result.postValue(
                UploadUserIdResult(
                    parameters,
                    Result.Error(IllegalArgumentException("user has no email address"))
                )
            )
            return
        }

        result.postValue(UploadUserIdResult(parameters, Result.Loading))

        val collection = firestore.collection(PATH_EMAILS)
        val doc = collection.document(email)
        doc.get().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                result.postValue(UploadUserIdResult(parameters, Result.Error(task.exception!!)))
                return@addOnCompleteListener
            }

            val snapshot = task.result
            val completion = fun(task2: Task<Void>) {
                result.postValue(
                    UploadUserIdResult(
                        parameters,
                        if (task2.isSuccessful) Result.success else Result.Error(task2.exception!!)
                    )
                )
            }
            if (!snapshot.exists()) {
                // Add a new doc with this field
                doc.set(mapOf(FIELD_FIREBASE_ID to parameters.uid))
                    .addOnCompleteListener(completion)
            } else if (!hasMatchingFirebaseId(parameters, snapshot)) {
                // Update the field in the doc
                doc.update(FIELD_FIREBASE_ID, parameters.uid)
                    .addOnCompleteListener(completion)
            }
        }
    }

    private fun hasMatchingFirebaseId(user: UserInfo, snapshot: DocumentSnapshot): Boolean {
        return (snapshot.get(FIELD_FIREBASE_ID) as? String) == user.uid
    }
}
