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

package com.example.androidthings.endtoend.companion.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.androidthings.endtoend.companion.auth.AuthProvider
import com.example.androidthings.endtoend.companion.data.Gizmo
import com.example.androidthings.endtoend.companion.data.GizmoDao
import com.google.firebase.auth.UserInfo

data class DaoArgs(val userInfo: UserInfo?, val gizmoId: String?)

class GizmoDetailViewModel(
    private val gizmoDao: GizmoDao,
    private val authProvider: AuthProvider
) : ViewModel(), AuthProvider by authProvider {

    // We need both the user info and the gizmo ID before we can load anything. One of these is
    // provided by another LiveData, so use a MediatorLiveData.
    private val daoArgs = MediatorLiveData<DaoArgs>()
    private val daoArgsObserver = Observer<DaoArgs> {
        loadGizmo(it)
    }

    // This LiveData comes from the DAO and can change whenever we have different arguments. We use
    // a MediatorLiveData to expose to clients, swapping out the source whenever we need to acquire
    // a new LiveData from the DAO.
    private var _gizmoLiveData: LiveData<Gizmo?>? = null
    private val _mediated = MediatorLiveData<Gizmo?>()
    val gizmoLiveData: LiveData<Gizmo?>
        get() = _mediated

    init {
        daoArgs.observeForever(daoArgsObserver)
        daoArgs.addSource(userLiveData) { user -> setUser(user) }
    }

    override fun onCleared() {
        super.onCleared()
        daoArgs.removeObserver(daoArgsObserver)
    }

    private fun setUser(userInfo: UserInfo?) {
        val args = daoArgs.value ?: DaoArgs(null, null)
        if (args.userInfo != userInfo) {
            daoArgs.value = args.copy(userInfo = userInfo)
        }
    }

    fun setGizmoId(gizmoId: String) {
        val args = daoArgs.value ?: DaoArgs(null, null)
        if (args.gizmoId != gizmoId) {
            daoArgs.value = args.copy(gizmoId = gizmoId)
        }
    }

    private fun loadGizmo(args: DaoArgs) {
        if (args.userInfo == null || args.gizmoId == null) {
            // missing required arguments
            return
        }

        _gizmoLiveData?.let {
            _mediated.removeSource(it)
        }
        val liveData = gizmoDao.getObservableGizmo(args.userInfo, args.gizmoId)
        _mediated.addSource(liveData) {
            _mediated.postValue(liveData.value)
        }
        _gizmoLiveData = liveData
    }
}
