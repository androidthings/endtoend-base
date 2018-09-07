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

package com.example.androidthings.endtoend.shared.data

import java.util.concurrent.Executors

val asyncExecutor = Executors.newFixedThreadPool(4)

// Firestore path elements
const val PATH_EMAILS = "emails"
const val PATH_USERS = "users"
const val PATH_GIZMOS = "gizmos"

// Firestore field names
const val FIELD_FIREBASE_ID = "firebaseId"
