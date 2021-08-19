/*
 * Copyright 2021 Green Mushroom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.gm.cleaner.plugin.app

import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import me.gm.cleaner.plugin.R
import rikka.material.widget.AppBarLayout

abstract class BaseFragment : Fragment() {
    lateinit var appBarLayout: AppBarLayout

    protected fun setAppBar(root: ViewGroup): Toolbar {
        appBarLayout = root.findViewById(R.id.toolbar_container)
        val toolbar: Toolbar = root.findViewById(R.id.toolbar)
        (requireActivity() as BaseActivity).setAppBar(appBarLayout, toolbar)
        return toolbar
    }

    protected fun navigateUp(): Boolean {
        try {
            NavHostFragment.findNavController(this).navigateUp()
            return true
        } catch (ignored: IllegalStateException) {
        }
        return false
    }
}