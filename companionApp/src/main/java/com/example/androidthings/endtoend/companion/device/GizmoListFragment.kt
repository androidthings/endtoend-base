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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.androidthings.endtoend.companion.R
import com.example.androidthings.endtoend.companion.ViewModelFactory
import com.example.androidthings.endtoend.shared.data.model.Gizmo
import com.example.androidthings.endtoend.shared.domain.Result
import com.example.androidthings.endtoend.shared.domain.Result.Loading
import com.example.androidthings.endtoend.shared.domain.successOr
import kotlinx.android.synthetic.main.fragment_gizmo_list.empty
import kotlinx.android.synthetic.main.fragment_gizmo_list.list
import kotlinx.android.synthetic.main.fragment_gizmo_list.progress

/** Fragment that shows the user's gizmos. */
class GizmoListFragment : Fragment() {

    private lateinit var gizmoListViewModel: GizmoListViewModel
    private lateinit var adapter: GizmoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gizmo_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Use the Activity here so that the view model is retained when new instances of this
        // fragment are created (e.g. by config changes)
        gizmoListViewModel = ViewModelProviders.of(requireActivity(), ViewModelFactory.instance)
            .get(GizmoListViewModel::class.java)

        gizmoListViewModel.gizmoListLiveData.observe(this, Observer { bindGizmoList(it) })
        gizmoListViewModel.selectedGizmoLiveData.observe(this, Observer { event ->
            val gizmoId = event.getContentIfNotHandled()?.id ?: return@Observer
            val directions = GizmoListFragmentDirections.navActionSelectGizmo()
                .setGizmoId(gizmoId)
            view?.findNavController()?.navigate(directions)
        })

        adapter = GizmoListAdapter(gizmoListViewModel)
        list.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.device_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sign_out) {
            gizmoListViewModel.performSignOut()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindGizmoList(result: Result<List<Gizmo>>) {
        val isLoading = result is Loading
        val gizmos = result.successOr(emptyList())
        adapter.submitList(gizmos)

        progress.visibility = if (isLoading) View.VISIBLE else View.GONE
        list.visibility = if (gizmos.isNotEmpty()) View.VISIBLE else View.GONE
        empty.visibility = if (!isLoading && gizmos.isEmpty()) View.VISIBLE else View.GONE
    }
}
