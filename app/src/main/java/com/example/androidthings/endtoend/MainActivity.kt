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

package com.example.androidthings.endtoend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.androidthings.endtoend.auth.FirebaseDeviceAuthenticator
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private val TAG: String = "MainActivity"

    lateinit var gpioManager: GpioManager

    lateinit var fcmReceiver: BroadcastReceiver

    lateinit var firebaseAuth: FirebaseDeviceAuthenticator

    val listener = object : OnLedStateChangedListener {
        override fun onStateChanged() {
            updateFirestore()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gpioManager = GpioManager(lifecycle, listener)

        gpioManager.initGpio()

        firebaseAuth = FirebaseDeviceAuthenticator()
        firebaseAuth.initAuth(this)

        FirestoreManager.init(this)
    }

    fun updateFirestore() {
        val newStates = arrayOf(gpioManager.leds[0].value,
            gpioManager.leds[1].value,
            gpioManager.leds[2].value)

        FirestoreManager.updateGizmoDocState(newStates)
    }

    fun initFcmReceiver() {
        fcmReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    val jsonCommandStr = intent.getStringExtra(FcmContract.COMMAND_KEY)

                    var cmds = JSONObject(jsonCommandStr)
                        .getJSONArray("inputs")
                        .getJSONObject(0)
                        .getJSONObject("payload")
                        .getJSONArray("commands")
                        .getJSONObject(0)
                        .getJSONArray("execution")
                        .getJSONObject(0)
                        .getJSONObject("params")
                        .getJSONObject("updateToggleSettings")

                    cmds.keys().forEach { key ->
                        val newState = cmds.getBoolean(key)
                        val ledIndex= FcmContract.LEDS.indexOf(key)
                        gpioManager.setLed(ledIndex, newState)
                    }

                    updateFirestore()


                }
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            fcmReceiver,
            IntentFilter(FcmContract.FCM_INTENT_ACTION)
        )
    }

    override fun onResume() {
        super.onResume()
        initFcmReceiver()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fcmReceiver)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.initAuth(this)
    }
}
