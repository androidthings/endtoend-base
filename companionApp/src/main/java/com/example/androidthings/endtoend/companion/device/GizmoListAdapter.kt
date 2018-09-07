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
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.androidthings.endtoend.companion.R
import com.example.androidthings.endtoend.shared.data.model.Gizmo

class GizmoListAdapter(
    private val viewModel: GizmoListViewModel
) : ListAdapter<Gizmo, GizmoViewHolder>(GizmoDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GizmoViewHolder {
        return GizmoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_gizmo, parent, false),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: GizmoViewHolder, position: Int) {
        holder.bindGizmo(getItem(position))
    }
}

class GizmoViewHolder(
    itemView: View,
    private val viewModel: GizmoListViewModel,
    private val nameView: TextView = itemView.findViewById(R.id.gizmo_name),
    private val typeView: TextView = itemView.findViewById(R.id.gizmo_secondary)
) : ViewHolder(itemView) {

    private lateinit var gizmo: Gizmo

    init {
        itemView.setOnClickListener {
            viewModel.selectGizmo(gizmo)
        }
    }

    internal fun bindGizmo(item: Gizmo) {
        gizmo = item
        nameView.text = gizmo.displayName
        typeView.text = gizmo.type
    }
}

private object GizmoDiff : DiffUtil.ItemCallback<Gizmo>() {
    override fun areItemsTheSame(oldItem: Gizmo, newItem: Gizmo) = (oldItem == newItem)

    override fun areContentsTheSame(oldItem: Gizmo, newItem: Gizmo): Boolean {
        return oldItem.displayName == newItem.displayName && oldItem.type == newItem.type
    }
}
