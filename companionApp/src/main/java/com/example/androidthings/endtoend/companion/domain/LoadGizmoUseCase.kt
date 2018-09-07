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
import com.example.androidthings.endtoend.shared.data.model.Gizmo
import com.example.androidthings.endtoend.companion.data.GizmoDao
import com.example.androidthings.endtoend.shared.domain.Result
import com.example.androidthings.endtoend.shared.domain.UseCase
import com.google.firebase.auth.UserInfo

/** UseCase that loads a Gizmo. This is a continuous UseCase. */
class LoadGizmoUseCase(
    private val gizmoDao: GizmoDao
) : UseCase<LoadGizmoUseCaseParameters, Result<Gizmo?>>() {

    private var currentParams = LoadGizmoUseCaseParameters(null, null)
    private var sourceLiveData: LiveData<Gizmo?>? = null

    override fun execute(parameters: LoadGizmoUseCaseParameters) {
        if (parameters == currentParams) {
            return // We're already observing this Gizmo
        }
        currentParams = parameters

        sourceLiveData?.let {
            result.removeSource(it)
        }

        if (parameters.userInfo == null || parameters.gizmoId == null) {
            // We don't have enough args, so skip to emitting a result
            sourceLiveData = null
            result.postValue(
                Result.Error(
                    IllegalArgumentException("Missing required UserInfo or Gizmo ID (String)")
                )
            )
        } else {
            try {
                sourceLiveData = gizmoDao.getObservableGizmo(
                    parameters.userInfo, parameters.gizmoId
                ).also { source ->
                    result.addSource(source) {
                        result.postValue(Result.Success(source.value))
                    }
                }
            } catch (e: Exception) {
                sourceLiveData = null
                result.postValue(Result.Error(e))
            }
        }
    }
}

data class LoadGizmoUseCaseParameters(
    val userInfo: UserInfo?,
    val gizmoId: String?
)
