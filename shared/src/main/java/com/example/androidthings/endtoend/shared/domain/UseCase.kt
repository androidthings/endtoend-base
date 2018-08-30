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

package com.example.androidthings.endtoend.shared.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.androidthings.endtoend.shared.util.DefaultScheduler
import com.example.androidthings.endtoend.shared.util.Scheduler

/**
 * Utility class for executing background work and publishing results in an observable LiveData.
 * Clients should call [observe] once to watch for results and call [execute] every time an action
 * should be performed. For continuous use cases, this is sufficient.
 *
 * For one-shot use cases (e.g. an http call for a result), you should additionally supply something
 * in the parameters and result that can be used to distinguish between different executions so that
 * clients can map the result back later.
 */
abstract class UseCase<in P, R> {

    /** Scheduler provided for convenience if this UseCase needs to move to another thread. */
    protected val scheduler: Scheduler = DefaultScheduler
    protected val result = MediatorLiveData<Result<R>>()

    fun observe(): LiveData<Result<R>> = result

    abstract fun execute(parameters: P)
}

/** Allows calls like useCase.execute() for UseCases with no paramters. */
fun <R> UseCase<Unit, R>.execute() = execute(Unit)
