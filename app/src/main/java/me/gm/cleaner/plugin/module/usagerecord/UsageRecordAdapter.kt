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

package me.gm.cleaner.plugin.module.usagerecord

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import me.gm.cleaner.plugin.R
import me.gm.cleaner.plugin.dao.mediaprovider.MediaProviderDeleteRecord
import me.gm.cleaner.plugin.dao.mediaprovider.MediaProviderInsertRecord
import me.gm.cleaner.plugin.dao.mediaprovider.MediaProviderQueryRecord
import me.gm.cleaner.plugin.dao.mediaprovider.MediaProviderRecord
import me.gm.cleaner.plugin.databinding.UsagerecordItemBinding
import me.gm.cleaner.plugin.di.GlideApp
import me.gm.cleaner.plugin.widget.makeSnackbarWithFullyDraggableContainer
import java.util.*
import java.util.Calendar.DAY_OF_YEAR
import java.util.Calendar.YEAR

class UsageRecordAdapter(private val fragment: UsageRecordFragment) :
    ListAdapter<MediaProviderRecord, UsageRecordAdapter.ViewHolder>(CALLBACK) {
    private val context = fragment.requireContext()
    private val clipboardManager by lazy { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(UsagerecordItemBinding.inflate(LayoutInflater.from(parent.context)))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val record = getItem(position)
        GlideApp.with(fragment)
            .load(record.packageInfo)
            .into(binding.icon)
        binding.title.text = record.packageInfo?.label
        binding.operation.text = when (record) {
            is MediaProviderQueryRecord -> fragment.getString(R.string.queried_at)
            is MediaProviderInsertRecord -> fragment.getString(R.string.inserted_at)
            is MediaProviderDeleteRecord -> fragment.getString(R.string.deleted_at)
            else -> throw IllegalArgumentException()
        } + run {
            val then = Calendar.getInstance().apply { timeInMillis = record.timeMillis }
            val now = Calendar.getInstance()
            val flags = DateUtils.FORMAT_NO_NOON or DateUtils.FORMAT_NO_MIDNIGHT or
                    DateUtils.FORMAT_ABBREV_ALL or when {
                then[YEAR] != now[YEAR] -> DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE
                then[DAY_OF_YEAR] != now[DAY_OF_YEAR] -> DateUtils.FORMAT_SHOW_DATE
                else -> DateUtils.FORMAT_SHOW_TIME
            }
            DateUtils.formatDateTime(context, record.timeMillis, flags)
        } + if (record.intercepted) {
            fragment.getString(R.string.intercepted)
        } else {
            ""
        }
        binding.record.text = record.dataList.first()
        val more = record.dataList.size - 1
        if (more == 0) {
            binding.more.isVisible = false
        } else {
            binding.more.isVisible = true
            binding.more.text = fragment.getString(R.string.and_more, more)
        }
        binding.root.setOnClickListener {
            val adapter = ArrayAdapter<CharSequence>(
                context, R.layout.usagerecord_popup_item, record.dataList
            )
            val listPopupWindow = ListPopupWindow(context)
            listPopupWindow.setAdapter(adapter)
            listPopupWindow.anchorView = binding.root
            listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                val data = adapter.getItem(position).toString()
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, data))
                makeSnackbarWithFullyDraggableContainer(
                    { fragment.requireActivity().findViewById(R.id.fully_draggable_container) },
                    fragment.requireView(), fragment.getString(R.string.copied, data),
                    Snackbar.LENGTH_SHORT
                ).show()
                listPopupWindow.dismiss()
            }
            listPopupWindow.show()
        }
    }

    class ViewHolder(val binding: UsagerecordItemBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val CALLBACK: DiffUtil.ItemCallback<MediaProviderRecord> =
            object : DiffUtil.ItemCallback<MediaProviderRecord>() {
                override fun areItemsTheSame(
                    oldItem: MediaProviderRecord, newItem: MediaProviderRecord
                ) = oldItem.timeMillis == newItem.timeMillis

                override fun areContentsTheSame(
                    oldItem: MediaProviderRecord, newItem: MediaProviderRecord
                ) = oldItem == newItem
            }
    }
}