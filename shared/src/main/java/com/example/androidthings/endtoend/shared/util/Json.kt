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

import org.json.JSONArray
import org.json.JSONObject

/**
 * Convenience fuctions for building JSON. Example:
 *
 * jsonObject(
 *   "title" to "this is a string",
 *   "flag" to true,
 *   "count" to 5,
 *   "arr" to jsonArray(
 *     "one hen",
 *     "two ducks",
 *     "three squawking geese"
 *   ),
 *   "obj" to jsonObject(
 *     "something" to someVariable,
 *     "somethingElse" to 42
 *   )
 * )
 */

/** Construct a JSONArray from objects in the given iterator. */
private fun jsonArray(args: Iterator<*>): JSONArray {
    val arr = JSONArray()
    for (arg in args) {
        arr.put(arg)
    }
    return arr
}

/** Construct a JSONArray from the given objects. */
fun jsonArray(vararg args: Any?) = jsonArray(args.iterator())

/** Construct a JSONArray from the given collection. */
fun jsonArray(args: Iterable<*>) = jsonArray(args.iterator())

/** Construct a JSONObject from objects in the given iterator. */
fun jsonObject(args: Iterator<Pair<String, *>>): JSONObject {
    val obj = JSONObject()
    for ((key, value) in args) {
        obj.put(key, value)
    }
    return obj
}

/** Construct a JSONObject from the given pairs. */
fun jsonObject(vararg args: Pair<String, *>) = jsonObject(args.iterator())

/** Construct a JSONObject from the given collection. */
fun jsonObject(args: Iterable<Pair<String, *>>) = jsonObject(args.iterator())
