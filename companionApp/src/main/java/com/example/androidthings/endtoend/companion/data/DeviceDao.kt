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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.concurrent.Executors

/** Data source for information about a user's devices. */
interface DeviceDao {
    fun getObservableDevices(userInfo: UserInfo): LiveData<List<Device>>
}

class FirestoreDeviceDao: DeviceDao {
    private val firestore = FirebaseFirestore.getInstance()

    override fun getObservableDevices(userInfo: UserInfo): LiveData<List<Device>> {
        // TODO maybe create one per uid in case multiple consumers want to observe
        val query = firestore.collection(PATH_USERS)
            .document("google-oauth2|112379635499756325744") // todo use uid from userInfo
            .collection(PATH_DEVICES)
        return FirestoreQueryLiveData(query, ::processSnapshot, asyncExecutor)
    }

    private fun processSnapshot(snapshot: QuerySnapshot): List<Device> {
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Device::class.java)?.apply {
                id = doc.id
            }
        }
    }
}

private val asyncExecutor = Executors.newFixedThreadPool(4)

// Firestore path elements
private const val PATH_USERS = "users"
private const val PATH_DEVICES = "devices"
