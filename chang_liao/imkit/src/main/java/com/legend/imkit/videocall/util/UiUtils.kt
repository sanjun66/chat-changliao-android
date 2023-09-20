package com.legend.imkit.videocall.util

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.annotation.*
import androidx.annotation.IntRange
import com.legend.base.Applications
import com.legend.imkit.R

private const val RANDOM_COLOR_START_RANGE = 0
private const val RANDOM_COLOR_END_RANGE = 9

fun getColorCircleDrawable(colorPosition: Int): Drawable {
    return getColoredCircleDrawable(getCircleColor(colorPosition % RANDOM_COLOR_END_RANGE))
}

fun getColoredCircleDrawable(@ColorInt color: Int): Drawable {
    val drawable = getDrawable(R.drawable.shape_circle) as GradientDrawable
    drawable.setColor(color)
    return drawable
}

fun getCircleColor(@IntRange(from = RANDOM_COLOR_START_RANGE.toLong(), to = RANDOM_COLOR_END_RANGE.toLong())
                   colorPosition: Int): Int {
    val colorIdName = String.format("random_color_%d", colorPosition + 1)
    val colorId = Applications.getCurrent().resources
            .getIdentifier(colorIdName, "color", Applications.getCurrent().packageName)

    return getColor(colorId)
}

fun getString(@StringRes stringId: Int): String {
    return Applications.getCurrent().getString(stringId)
}

fun getDrawable(@DrawableRes drawableId: Int): Drawable {
    return Applications.getCurrent().resources.getDrawable(drawableId)
}

fun getColor(@ColorRes colorId: Int): Int {
    return Applications.getCurrent().resources.getColor(colorId)
}

fun getDimen(@DimenRes dimenId: Int): Int {
    return Applications.getCurrent().resources.getDimension(dimenId).toInt()
}