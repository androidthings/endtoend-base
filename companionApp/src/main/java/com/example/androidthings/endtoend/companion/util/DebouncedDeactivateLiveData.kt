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

package com.example.androidthings.endtoend.companion.util

import android.os.Handler
import androidx.lifecycle.MutableLiveData

/**
 * LiveData that briefly delays deactivation to avoid thrashing [onInactive] and [onActive] when
 * observers are removed and added rapidly, e.g. by a configuration change. This is useful if there
 * are significant ramifications associated with becoming active or inactive that are best avoided
 * if the state will revert back rapidly.
 *
 * Subclasses implement [onActivate] and [onDeactivate] instead.
 */
open class DebouncedDeactivateLiveData<T> : MutableLiveData<T>() {

    companion object {
        private val handler = Handler()
    }

    private var inactivePending = false

    final override fun onActive() {
        super.onActive()
        if (inactivePending) {
            inactivePending = false
        } else {
            onActivate()
        }
    }

    final override fun onInactive() {
        super.onInactive()
        inactivePending = true
        handler.postDelayed(::checkDeactivate, 1000L)
    }

    private fun checkDeactivate() {
        if (inactivePending && !hasActiveObservers()) {
            onDeactivate()
        }
        inactivePending = false
    }

    /** Called when this LiveData becomes active. */
    open fun onActivate() {}

    /** Called when this LiveData becomes inactive. */
    open fun onDeactivate() {}
}
