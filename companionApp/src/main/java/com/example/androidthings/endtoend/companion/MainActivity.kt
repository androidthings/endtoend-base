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

package com.example.androidthings.endtoend.companion

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.androidthings.endtoend.companion.auth.AuthViewModel
import com.example.androidthings.endtoend.companion.auth.AuthViewModel.AuthResult.CANCEL
import com.example.androidthings.endtoend.companion.auth.AuthViewModel.AuthResult.FAIL
import com.example.androidthings.endtoend.companion.auth.AuthViewModel.AuthResult.SUCCESS
import com.example.androidthings.endtoend.companion.auth.FirebaseAuthProvider
import com.example.androidthings.endtoend.companion.auth.FirebaseAuthUiHelper
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse

class MainActivity : AppCompatActivity(), FirebaseAuthUiHelper {

    companion object {
        private const val REQUEST_SIGN_IN = 42
        private val signInProviders = listOf(AuthUI.IdpConfig.GoogleBuilder().build())
    }

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseAuthProvider.setAuthUiHelper(this)
        authViewModel = ViewModelProviders.of(this, ViewModelFactory.instance)
            .get(AuthViewModel::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseAuthProvider.unsetAuthUiHelper(this)
    }

    // -- FirebaseAuthUiHelper

    override fun performSignIn() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(signInProviders)
                .build(),
            REQUEST_SIGN_IN
        )
    }

    override fun performSignOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener { task ->
                authViewModel.onAuthResult(when {
                    task.isSuccessful -> SUCCESS
                    task.isCanceled -> CANCEL
                    else -> FAIL
                })
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SIGN_IN) {
            val idpResponse = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                authViewModel.onAuthResult(SUCCESS)
            } else {
                authViewModel.onAuthResult(if (idpResponse == null) CANCEL else FAIL)
            }
        }
    }
}
