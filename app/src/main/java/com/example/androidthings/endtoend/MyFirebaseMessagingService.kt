/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.endtoend

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    private val prefKey = "fcmKeystore"

    private val fcmTokenKey = "fcmKey"


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "payload is: " + remoteMessage.data.toString())

        val intent = Intent().apply {
            action = FcmContract.FCM_INTENT_ACTION
            val jsonPayload = remoteMessage.data[FcmContract.FCM_PAYLOAD_KEY]
            putExtra(FcmContract.COMMAND_KEY, jsonPayload)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // Store FCM token locally
        val sharedPreferences =  getSharedPreferences( prefKey, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString(fcmTokenKey, token)
        }

        // Send FCM token up to firestore in order to send commands down to device
        sendRegistrationToServer(token)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        FirestoreManager.updateFcmToken(token)
    }
}