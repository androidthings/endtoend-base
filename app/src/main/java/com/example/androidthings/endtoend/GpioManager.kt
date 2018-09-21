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
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager

interface OnLedStateChangedListener {
    fun onStateChanged ()
}

class GpioManager(val lifecycle: Lifecycle, var listener : OnLedStateChangedListener) {

    val TAG: String = "GpioManager"

    // The pin names for each dev board are available in the hardware section of the Android Things
    // site at https://developer.android.com/things/hardware
    // e.g the IMX7d pinout is at https://developer.android.com/things/hardware/imx7d-pico-io
    val BUTTON_PINS = arrayOf(
        "GPIO6_IO14",
        "GPIO6_IO15",
        "GPIO2_IO07"
    )

    val LED_PINS = arrayOf(
        "GPIO2_IO02",
        "GPIO2_IO00",
        "GPIO2_IO05"
    )

    lateinit var leds: Array<Gpio>

    fun initGpio() {

        val buttons: Array<Gpio> = Array(BUTTON_PINS.size) { i ->
            initButton(BUTTON_PINS[i])
        }

        leds = Array(LED_PINS.size) { i ->
            initLed(LED_PINS[i])
        }

        for (button in buttons) {
            button.addToLifecycle(lifecycle, ::initButton)
        }

        for ((index, led: Gpio) in leds.withIndex()) {
            leds[index] = leds[index].addToLifecycle(lifecycle, ::initLed)
        }

        // Debug, just a visual indicator that everything booted up as expected.
        leds[0].value = true

        // Attach LED toggle as a visual indicator to each corresponding button.
        for ((index, button: Gpio) in buttons.withIndex()) {
            button.registerGpioCallback {
                Log.i(TAG, "Button $index Pressed.")
                toggleLed(index)
                listener.onStateChanged()
                return@registerGpioCallback true
            }
        }
    }

    fun toggleLed(ledNum: Int) {
        setLed(ledNum, !leds[ledNum].value)
    }

    fun setLed(ledNum: Int, isOn : Boolean) {
        leds[ledNum].value = isOn
    }

    fun initLed(pinName: String): Gpio {
        val ledGpio = PeripheralManager.getInstance().openGpio(pinName)
        // Configure as an output.
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        return ledGpio
    }

    fun initButton(pinName: String): Gpio {
        val buttonGpio: Gpio = PeripheralManager.getInstance().openGpio(pinName)
        buttonGpio.setDirection(Gpio.DIRECTION_IN)
        buttonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING)
        buttonGpio.registerGpioCallback {
            Log.i(TAG, "GPIO changed, button pressed")
            // Return true to continue listening to events
            true
        }

        return buttonGpio
    }

    /* Helper object which uses Delegates to "add" LifecycleObserver interface to GPIO. With
     * multiple GPIO peripherals (sensors, lights, buttons, etc), an Activity's onStart/onStop
     * methods can get large and repetitive very quickly.  By using Android Lifecycles most of this
     * can be avoided, by making each individual component listen for lifecycle changes and respond
     * accordingly. The below lines will release a GPIO pin when a LifeCycleOwner
     * (usually an Activity) fires its onStop method, and re-initialize the GPIO when onStart is
     * fired.
     * Info on Lifecycle framework is available at:
     * https://developer.android.com/topic/libraries/architecture/lifecycle
    */
    class LifecycleAwareGpio(
        var gpio: Gpio
    ) : Gpio by gpio, LifecycleObserver {

        var closed: Boolean = false

        private val TAG = "LifecycleGpio"

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

    fun Gpio.addToLifecycle(lifecycle: Lifecycle, gpioInit: (String) -> Gpio): Gpio {
        val observer: Gpio = LifecycleAwareGpio(this)
        lifecycle.addObserver(observer as LifecycleObserver)
        return observer
    }
}
