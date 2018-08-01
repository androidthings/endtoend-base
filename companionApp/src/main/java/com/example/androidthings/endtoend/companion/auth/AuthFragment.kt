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

package com.example.androidthings.endtoend.companion.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.androidthings.endtoend.companion.R
import com.example.androidthings.endtoend.companion.ViewModelFactory
import com.example.androidthings.endtoend.companion.auth.AuthViewModel.AuthUiModel
import kotlinx.android.synthetic.main.fragment_auth.auth_text
import kotlinx.android.synthetic.main.fragment_auth.progress
import kotlinx.android.synthetic.main.fragment_auth.sign_in
import kotlinx.android.synthetic.main.fragment_auth.sign_out

/**
 * Fragment showing UI for the user to initiate authentication flow. Note that the actual auth flow
 * may be implemented elsewhere.
 */
class AuthFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.sign_in).setOnClickListener {
            authViewModel.signIn()
        }
        view.findViewById<View>(R.id.sign_out).setOnClickListener {
            authViewModel.signOut()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        authViewModel = ViewModelProviders.of(requireActivity(), ViewModelFactory.instance)
            .get(AuthViewModel::class.java)

        authViewModel.authUiModelLiveData.observe(this, Observer { model -> bindAuthUi(model) })
    }

    private fun bindAuthUi(model: AuthUiModel) {
        if (model.initializing) {
            progress.visibility = View.VISIBLE
            sign_in.visibility = View.GONE
            sign_out.visibility = View.GONE
            auth_text.visibility = View.GONE
        } else {
            progress.visibility = View.GONE
            sign_in.visibility = View.VISIBLE
            sign_out.visibility = View.VISIBLE
            auth_text.visibility = View.VISIBLE

            sign_in.isEnabled = false
            sign_out.isEnabled = false
            if (!model.authInProgress) {
                sign_in.isEnabled = model.user == null
                sign_out.isEnabled = model.user != null
            }

            if (model.user != null) {
                auth_text.text = getString(R.string.signed_in_as, model.user.displayName)
            } else {
                auth_text.setText(R.string.not_signed_in)
            }
        }
    }
}
