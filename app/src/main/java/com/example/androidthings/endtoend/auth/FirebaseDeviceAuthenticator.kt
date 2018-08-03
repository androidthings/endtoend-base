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

package com.example.androidthings.endtoend.auth
import android.app.Activity
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseDeviceAuthenticator(val lifecycle : Lifecycle? = null) {

    private var firebaseAuth = FirebaseAuth.getInstance()

    private val TAG = "FirebaseDeviceAuth"
    fun initAuth(activity : Activity) {
        var currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // User is signed in
            obtainUserToken(currentUser)
        } else {
            firebaseAuth.signInAnonymously()
                .addOnCompleteListener(activity) { task : Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Signed in ")
                        currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {

                            obtainUserToken(currentUser as FirebaseUser)
                        }
                    } else {
                        Log.d(TAG, "Signin failed: ${task.exception}")

                    }
                }
        }
    }

    fun obtainUserToken(user: FirebaseUser) {
        user.getIdToken(false).addOnCompleteListener { result ->
            Log.d(TAG, "Token is: ${result.result.token}")
        }
    }

}