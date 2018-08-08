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

package com.example.androidthings.endtoend.companion.device

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.androidthings.endtoend.companion.R
import com.example.androidthings.endtoend.companion.data.Device

class DeviceListAdapter : ListAdapter<Device, DeviceViewHolder>(DeviceDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_device, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bindDevice(getItem(position))
    }
}

class DeviceViewHolder(
    itemView: View,
    private val nameView: TextView = itemView.findViewById(R.id.device_name),
    private val typeView: TextView = itemView.findViewById(R.id.device_secondary)
) : ViewHolder(itemView) {

    private lateinit var device: Device

    init {
        itemView.setOnClickListener {
            Log.d("DeviceViewHolder", "Clicked ${device.name}") // TODO navigate to device detail
        }
    }

    fun bindDevice(item: Device) {
        device = item
        nameView.text = device.name
        typeView.text = device.type
    }
}

object DeviceDiff : DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device) = (oldItem == newItem)

    override fun areContentsTheSame(oldItem: Device, newItem: Device) =
        oldItem.areContentsTheSame(newItem)
}
