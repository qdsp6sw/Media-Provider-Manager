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

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import kotlin.math.max

open class MeasureOrderReversedHorizontalLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        check(orientation == HORIZONTAL)
        var maxHeight = 0
        var totalWidth = 0
        var childState = 0

        for (i in (0 until childCount).reversed()) {
            val child = getChildAt(i)
            if (!child.isVisible) {
                continue
            }
            val freeWidthSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec) - totalWidth, MeasureSpec.AT_MOST
            )
            child.measure(freeWidthSpec, heightMeasureSpec)
            val lp = child.layoutParams as MarginLayoutParams
            maxHeight = max(maxHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
            totalWidth += child.measuredWidth + lp.leftMargin + lp.rightMargin
            childState = combineMeasuredStates(childState, child.measuredState)
        }
        // Add in our padding
        totalWidth += paddingLeft + paddingRight
        // Check against our minimum width
        val widthSize = max(totalWidth, suggestedMinimumWidth)
        // Reconcile our calculated size with the widthMeasureSpec
        val widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec, 0)
        setMeasuredDimension(
            widthSizeAndState or (childState and MEASURED_STATE_MASK),
            resolveSizeAndState(
                maxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }
}