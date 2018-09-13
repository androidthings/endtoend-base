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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.androidthings.endtoend.companion.R
import com.example.androidthings.endtoend.companion.data.model.ToggleDetail


class ToggleListAdapter(
    private val viewModel: GizmoDetailViewModel
) : ListAdapter<ToggleDetail, ToggleViewHolder>(ToggleDetailDiff) {

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
    private val statusView: TextView = itemView.findViewById(R.id.toggle_status),
    private val progressView: ProgressBar = itemView.findViewById(R.id.toggle_progress)
) : ViewHolder(itemView) {

    private lateinit var toggleDetail: ToggleDetail

    init {
        itemView.setOnClickListener {
            viewModel.onToggleClicked(toggleDetail.toggle)
        }
    }

    internal fun bind(item: ToggleDetail) {
        toggleDetail = item

        nameView.text = item.toggle.displayName
        iconView.setImageResource(
            if (item.toggle.on) R.drawable.ic_lightbulb_filled else R.drawable.ic_lightbulb_outline
        )
        statusView.text = if (item.toggle.on) "ON" else "OFF"

        if (item.progress) {
            progressView.visibility = View.VISIBLE
            statusView.visibility = View.GONE
            itemView.isClickable = false
        } else {
            progressView.visibility = View.GONE
            statusView.visibility = View.VISIBLE
            itemView.isClickable = true
        }
    }
}

object ToggleDetailDiff : DiffUtil.ItemCallback<ToggleDetail>() {
    override fun areItemsTheSame(oldItem: ToggleDetail, newItem: ToggleDetail) =
        (oldItem == newItem)

    override fun areContentsTheSame(oldItem: ToggleDetail, newItem: ToggleDetail): Boolean {
        val oldToggle = oldItem.toggle
        val newToggle = newItem.toggle
        return oldToggle.displayName == newToggle.displayName &&
            oldToggle.on == newToggle.on &&
            oldItem.progress == newItem.progress
    }
}
