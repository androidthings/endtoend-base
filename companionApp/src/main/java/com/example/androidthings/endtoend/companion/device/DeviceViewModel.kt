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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.androidthings.endtoend.companion.auth.AuthProvider
import com.example.androidthings.endtoend.companion.data.Device
import com.example.androidthings.endtoend.companion.data.DeviceDao

class DeviceViewModel(
    private val deviceDao: DeviceDao,
    private val authProvider: AuthProvider
) : ViewModel(), AuthProvider by authProvider {

    val deviceLiveData: LiveData<List<Device>>

    init {
        // Creates a live data backed by another. The backing LiveData is replaced whenever the user
        // changes, without observers having to resubscribe to ours.
        deviceLiveData = Transformations.switchMap(userLiveData) { user ->
            if (user != null) {
                deviceDao.getObservableDevices(user)
            } else {
                // Emit an empty list once.
                MutableLiveData<List<Device>>().apply {
                    value = emptyList()
                }
            }
        }
    }

    fun selectDevice(device: Device) {
        TODO("not implemented")
    }
}
