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
import android.util.Log
import androidx.core.content.edit
import com.example.androidthings.endtoend.shared.data.model.Gizmo
import com.example.androidthings.endtoend.shared.data.model.Toggle
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*

object FirestoreManager {

    // todo : get ID from firebase function at device creation

    private var gizmoDocId: String? = null

    private const val TAG = "FirestoreManager"

    private const val prefsKey = "firestorePrefsKey"
    private const val firestoreDocIdKey = "docIdKey"
    private const val deviceIdKey = "deviceId"
    private const val fcmTokenKey = "fcmToken"
    private const val togglesKey = "toggles"

    private val pendingData: MutableMap<String, Any> = HashMap()

    // todo: get userid info from companion app
    private const val userId = "miZjm4sqa5h2FIuzhOnyaq6HKF53"

    fun init(context: Context) {
        val docId = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
            .getString(firestoreDocIdKey, "") ?: ""
        if (!docId.isEmpty()) {
            gizmoDocId = docId
            Log.d(TAG, "Using previously stored doc ID : $gizmoDocId")
        } else {
            createGizmoDoc(context)
        }
    }

    private fun lookupGizmoDocument(id: String): DocumentReference {

        val firestore = FirebaseFirestore.getInstance()
        return firestore.collection("users")
            .document(userId)
            .collection("gizmos")
            .document(id)
    }

    private fun genToggleList(newVals : Array<Boolean>? = null) : List<Toggle>{
        return listOf(
            Toggle(id = FcmContract.LEDS[0], displayName = "Red LED", on = newVals?.get(0) ?: true),
            Toggle(id = FcmContract.LEDS[1], displayName = "Green LED", on = newVals?.get(1) ?: true),
            Toggle(id = FcmContract.LEDS[2], displayName = "Blue LED", on = newVals?.get(2) ?: true))
    }

    private fun generateGizmo(toggles : List<Toggle>? = null) : Gizmo {
        val gizmo = Gizmo()
        gizmo.name = "Magic Rainbow Hat"
        gizmo.nicknames = listOf("Hattie McHatHat")
        gizmo.toggles = genToggleList()

        gizmo.toggles = toggles ?: genToggleList()

        return gizmo
    }

    private fun createGizmoDoc(context: Context) {
        val firestore = FirebaseFirestore.getInstance()
        val gizmo = generateGizmo()
        gizmo.id = getDeviceId(context)


        firestore.collection("users")
            .document(userId)
            .collection("gizmos")
            .add(gizmo)
            .addOnSuccessListener { doc ->
                gizmoDocId = doc.id
                storeDocumentId(context, doc.id)
                if (pendingData.isNotEmpty()) {
                    if (pendingData.containsKey(fcmTokenKey)) {
                        updateFcmToken(pendingData.get(fcmTokenKey) as String)
                    }
                }
                Log.d(TAG, "Successfully created Gizmo ${doc.id}")
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed!", it)
            }
    }

    fun updateGizmoDocState(newVals : Array<Boolean>) {

        val docId = gizmoDocId ?: return
        val docRef = lookupGizmoDocument(docId)

        val toggles = genToggleList(newVals)
        val gizmo = generateGizmo(toggles)

        docRef.set(gizmo, SetOptions.mergeFields(togglesKey))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Success updating Toggles!")

                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
    }

    private fun storeDocumentId(context: Context, docId: String) {
        val sharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString(firestoreDocIdKey, docId)
        }
    }

    fun updateFcmToken(token: String) {
        // If the FCM token gets received before firestore document is initialized,
        // store the gizmo ID in a queue for pending data to update.
        // Otherwise, update the document immediately
        val gizmo = gizmoDocId
        if (gizmo == null) {
            Log.d(TAG, "Adding Gizmo to pending : There's no ID for it yet.")
            pendingData[fcmTokenKey] = token
            return
        }
        Log.d(TAG, "Have an ID, updating gizmo with fcm token.")
        val data: MutableMap<String, Any> = HashMap()
        data[fcmTokenKey] = token
        lookupGizmoDocument(gizmo)
            .set(data, SetOptions.merge())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Success updating FCM token!")
                    if (pendingData.contains(fcmTokenKey)) {
                        pendingData.remove(fcmTokenKey)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
    }

    private fun getDeviceId(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(deviceIdKey)) {
            sharedPreferences.getString(deviceIdKey, "") ?: ""
        } else {
            val uniqueId = UUID.randomUUID().toString()
            sharedPreferences.edit {
                putString(deviceIdKey, uniqueId)
            }
            uniqueId
        }
    }
}