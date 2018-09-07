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

import androidx.lifecycle.LiveData
import com.example.androidthings.endtoend.companion.data.GizmoDao
import com.example.androidthings.endtoend.shared.data.model.Gizmo
import com.example.androidthings.endtoend.shared.domain.Result
import com.example.androidthings.endtoend.shared.domain.UseCase
import com.google.firebase.auth.UserInfo

/** UseCase that loads all Gizmos tied to the current user. This is a continuous UseCase. */
class LoadUserGizmosUseCase(
    private val gizmoDao: GizmoDao
) : UseCase<UserInfo?, Result<List<Gizmo>>>() {

    private var currentUserInfo: UserInfo? = null
    private var sourceLiveData: LiveData<List<Gizmo>>? = null

    override fun execute(parameters: UserInfo?) {
        if (currentUserInfo == parameters) {
            return // We're already observing this user
        }
        currentUserInfo = parameters

        // Different user; remove the old source
        sourceLiveData?.let {
            result.removeSource(it)
        }

        if (parameters == null) {
            // DAO requires a user, so we can skip to emitting a result
            sourceLiveData = null
            result.postValue(Result.Success(emptyList()))
        } else {
            result.postValue(Result.Loading) // First emit that we are loading

            try {
                // Start querying and add the new source
                sourceLiveData = gizmoDao.getObservableGizmos().also { source ->
                    result.addSource(source) { value ->
                        result.postValue(
                            if (value == null) {
                                Result.Loading
                            } else {
                                Result.Success(value)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                sourceLiveData = null
                result.postValue(Result.Error(e))
            }
        }
    }
}
