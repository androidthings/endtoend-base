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

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager


class GpioStuff(val lifecycle : Lifecycle? = null) {

    val TAG: String = "GpioStuff"

    // The pin names for each dev board are available in the hardware section of the Android Things
// site at https://developer.android.com/things/hardware
// e.g the IMX7d pinout is at https://developer.android.com/things/hardware/imx7d-pico-io
    val BUTTON_1_PIN = "GPIO6_IO14"
    val BUTTON_2_PIN = "GPIO6_IO15"
    val BUTTON_3_PIN = "GPIO2_IO07"

    val LED_1_PIN = "GPIO2_IO02"
    val LED_2_PIN = "GPIO2_IO00"
    val LED_3_PIN = "GPIO2_IO05"

    fun initGpio() {
        val button1: Gpio = initButton(BUTTON_1_PIN)
        val button2: Gpio = initButton(BUTTON_2_PIN)
        val button3: Gpio = initButton(BUTTON_3_PIN)

        val led1: Gpio = initLed(LED_1_PIN)
        val led2: Gpio = initLed(LED_2_PIN)
        val led3: Gpio = initLed(LED_3_PIN)

        if (lifecycle != null) {
            button1.addToLifecycle(lifecycle, ::initButton)
            button2.addToLifecycle(lifecycle, ::initButton)
            button3.addToLifecycle(lifecycle, ::initButton)

            led1.addToLifecycle(lifecycle, ::initLed)
            led2.addToLifecycle(lifecycle, ::initLed)
            led3.addToLifecycle(lifecycle, ::initLed)
        }

        led1.value = true

        button1.registerGpioCallback {
            Log.i(ContentValues.TAG, "Button A Pressed.")
            led1.value = !led1.value
            return@registerGpioCallback true
        }

        button2.registerGpioCallback {
            Log.i(ContentValues.TAG, "Button B Pressed.")
            led2.value = !led2.value
            return@registerGpioCallback true
        }

        button3.registerGpioCallback {
            Log.i(ContentValues.TAG, "Button C Pressed.")
            led3.value = !led3.value
            return@registerGpioCallback true
        }
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

    // Helper object which uses Delegates to "add" LifecycleObserver interface to GPIO.
// With multiple GPIO peripherals (sensors, lights, buttons, etc), an Activity's onStart/onStop methods
// can get large and repetitive very quickly.  By using Android Lifecycles most of this can be
// avoided, by making each individual component listen for lifecycle changes and respond accordingly.
// The below lines will release a GPIO pin when a LifeCycleOwner (usually an Activity) fires its onStop
// method, and re-initialize the GPIO when onStart is fired.
// Info on Lifecycle framework is available at: https://developer.android.com/topic/libraries/architecture/lifecycle
    class LifecycleAwareGpio(
        var gpio: Gpio,
        val gpioInit: (String) -> Gpio,
        var pinName: String = gpio.name
    ) : Gpio by gpio, LifecycleObserver {

        var closed: Boolean = false

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            close()
            Log.i(TAG, "Gpio closed.")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            if (closed) {
                gpio = gpioInit(pinName)
                pinName = gpio.name
                closed = false
            }
        }

        override fun close() {
            gpio.close()
            closed = true
        }
    }

    fun Gpio.addToLifecycle(lifecycle: Lifecycle, gpioInit: (String) -> Gpio): Gpio {
        val observer: Gpio = LifecycleAwareGpio(this, gpioInit)
        lifecycle.addObserver(observer as LifecycleObserver)
        return observer
    }
}
