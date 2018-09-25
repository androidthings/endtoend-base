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

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.button.Button.LogicState.PRESSED_WHEN_LOW
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager

interface OnLedStateChangedListener {
    fun onStateChanged()
}

class GpioManager(
    private val lifecycle: Lifecycle,
    private var listener: OnLedStateChangedListener
) {

    private val TAG: String = "GpioManager"

    // The pin names for each dev board are available in the hardware section of the Android Things
    // site at https://developer.android.com/things/hardware
    // e.g the IMX7d pinout is at https://developer.android.com/things/hardware/imx7d-pico-io
    private val buttonPins = arrayOf(
        "GPIO6_IO14",
        "GPIO6_IO15",
        "GPIO2_IO07"
    )

    private val ledPins = arrayOf(
        "GPIO2_IO02",
        "GPIO2_IO00",
        "GPIO2_IO05"
    )

    lateinit var leds: Array<Gpio>

    fun initGpio() {

        leds = Array(ledPins.size) { i ->
            initLed(ledPins[i])
        }

        Array(buttonPins.size) { i ->
            initButton(i)
        }

        // Debug, just a visual indicator that everything booted up as expected.
        leds[0].value = true
    }

    private fun toggleLed(ledNum: Int) {
        setLed(ledNum, !leds[ledNum].value)
    }

    fun setLed(ledNum: Int, isOn: Boolean) {
        leds[ledNum].value = isOn
    }

    private fun initLed(pinName: String): Gpio {
        val ledGpio =
            LifecycleAwareGpio(PeripheralManager.getInstance().openGpio(pinName), lifecycle)
        // Configure as an output.
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        return ledGpio
    }

    private fun initButton(index: Int): Button {
        return LifecycleAwareButton(
            lifecycle,
            buttonPins[index],
            Button.OnButtonEventListener { _, pressed ->
                if (pressed) {
                    Log.i(TAG, "Button $index Pressed.")
                    toggleLed(index)
                    listener.onStateChanged()
                }
            })
    }

    /* Helper object which uses Delegates to "add" LifecycleObserver interface to GPIO. With
     * multiple GPIO peripherals (sensors, lights, buttons, etc), an Activity's onStart/onStop
     * methods can get large and repetitive very quickly.
     * Info on Lifecycle framework is available at:
     * https://developer.android.com/topic/libraries/architecture/lifecycle
    */
    class LifecycleAwareGpio(
        private var gpio: Gpio,
        lifecycle: Lifecycle
    ) : Gpio by gpio, LifecycleObserver {

        var closed: Boolean = false

        private val TAG = "LifecycleGpio"

        init {
            lifecycle.addObserver(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            close()
            Log.i(TAG, "Gpio closed.")
        }

        override fun close() {
            gpio.close()
            closed = true
        }
    }

    class LifecycleAwareButton(
        lifecycle: Lifecycle,
        pinName: String,
        callback: OnButtonEventListener
    ) : Button(pinName, PRESSED_WHEN_LOW) {

        init {
            setOnButtonEventListener(callback)
            lifecycle.addObserver(this as LifecycleObserver)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            close()
        }
    }
}
