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

/**
 * Wrapper for state exposed via LiveData but which represents an event. See
 * [https://medium.com/google-developers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150]
 */
open class Event<out T>(private val content: T) {

    var handled = false
        private set // allow external read but not write

    /** Return the content if it hasn't been handled and mark it handled. Otherwise return null. */
    fun getContentIfNotHandled(): T? {
        return if (handled) null else content.also { handled = true }
    }

    /** Return the content, even if it has been handled. */
    fun peekContent() = content
}
