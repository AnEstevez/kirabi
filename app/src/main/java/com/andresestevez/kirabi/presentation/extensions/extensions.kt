package com.andresestevez.kirabi.presentation.extensions

import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import com.skydoves.rainbow.Rainbow
import com.skydoves.rainbow.RainbowOrientation
import com.skydoves.rainbow.color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun View.startBackgroundDrawableTransition(drawable: Drawable, duration: Int = 400) =
    withContext(Dispatchers.Main) {
        val transitionDrawable =
            TransitionDrawable(arrayOf(this@startBackgroundDrawableTransition.background, drawable))
        this@startBackgroundDrawableTransition.background = transitionDrawable
        transitionDrawable.startTransition(duration)
    }

fun View.createGradientDrawable(
    color1: Int,
    color2: Int,
    orientation: RainbowOrientation = RainbowOrientation.DIAGONAL_TOP_LEFT,
) = Rainbow(this@createGradientDrawable).palette {
    +color(color1)
    +color(color1)
    +color(color2)
}.getDrawable(orientation)