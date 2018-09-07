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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.androidthings.endtoend.companion.R
import com.example.androidthings.endtoend.shared.data.model.Toggle

class ToggleListAdapter(
    private val viewModel: GizmoDetailViewModel
) : ListAdapter<Toggle, ToggleViewHolder>(ToggleDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToggleViewHolder {
        return ToggleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_led_toggle, parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: ToggleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ToggleViewHolder(
    itemView: View,
    private val viewModel: GizmoDetailViewModel,
    private val nameView: TextView = itemView.findViewById(R.id.toggle_name),
    private val iconView: ImageView = itemView.findViewById(R.id.toggle_icon),
    private val statusView: TextView = itemView.findViewById(R.id.toggle_status)
) : ViewHolder(itemView) {

    private lateinit var toggle: Toggle

    init {
        itemView.setOnClickListener {
            viewModel.onToggleClicked(toggle)
        }
    }

    internal fun bind(item: Toggle) {
        toggle = item

        nameView.text = item.displayName
        iconView.setImageResource(
            if (item.on) R.drawable.ic_lightbulb_filled else R.drawable.ic_lightbulb_outline
        )
        statusView.text = if (item.on) "ON" else "OFF"
    }
}

object ToggleDiff : DiffUtil.ItemCallback<Toggle>() {
    override fun areItemsTheSame(oldItem: Toggle, newItem: Toggle) = (oldItem == newItem)

    override fun areContentsTheSame(oldItem: Toggle, newItem: Toggle): Boolean {
        return (oldItem.displayName == newItem.displayName && oldItem.on == newItem.on)
    }
}
