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

package com.example.androidthings.endtoend.shared.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val NUMBER_OF_THREADS = 4

interface Scheduler {

    fun execute(task: () -> Unit)

    fun executeDelayed(delayMillis: Long, task: () -> Unit)
}

/**
 * Scheduler that by default delegates operations to an [AsyncScheduler].
 */
object DefaultScheduler : Scheduler {

    private var delegate: Scheduler = AsyncScheduler

    /** Sets the new delegate scheduler. Pass null to revert to the default async one. */
    fun setDelegate(newDelegate: Scheduler?) {
        delegate = newDelegate ?: AsyncScheduler
    }

    override fun execute(task: () -> Unit) {
        delegate.execute(task)
    }

    override fun executeDelayed(delayMillis: Long, task: () -> Unit) {
        delegate.executeDelayed(delayMillis, task)
    }
}

/**
 * Scheduler that runs tasks in a [ExecutorService] with a fixed thread pool.
 */
object AsyncScheduler : Scheduler {

    private val executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS)

    override fun execute(task: () -> Unit) {
        executorService.execute(task)
    }

    override fun executeDelayed(delayMillis: Long, task: () -> Unit) {
        executorService.schedule(task, delayMillis, MILLISECONDS)
    }
}

/**
 * Scheduler that runs tasks synchronously. It does not support delayed execution.
 */
object SyncScheduler : Scheduler {

    override fun execute(task: () -> Unit) {
        task()
    }

    override fun executeDelayed(delayMillis: Long, task: () -> Unit) {
        task()
    }
}
