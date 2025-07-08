package com.example.project180.Util

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class BookPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        when {
            position < -1 -> { // [-Infinity,-1)
                page.alpha = 0f
            }
            position <= 0 -> { // [-1,0]
                page.alpha = 1 + position
                page.translationX = -page.width * position
                page.scaleX = 1f
                page.scaleY = 1f
            }
            position <= 1 -> { // (0,1]
                page.alpha = 1 - position
                page.translationX = -page.width * position
                val scaleFactor = 0.75f + (1 - 0.75f) * (1 - Math.abs(position))
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
            }
            else -> { // (1,+Infinity]
                page.alpha = 0f
            }
        }
    }
}