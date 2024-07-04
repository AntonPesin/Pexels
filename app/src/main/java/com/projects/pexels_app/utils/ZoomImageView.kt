package com.projects.pexels_app.utils

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), ScaleGestureDetector.OnScaleGestureListener {

    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private var scaleFactor = 1.0f
    private var matrixScale = Matrix()

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            resetZoom()
        }
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scaleFactor *= detector.scaleFactor
        scaleFactor = max(1.0f, min(scaleFactor, 5.0f))
        matrixScale.setScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
        imageMatrix = matrixScale
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    private fun resetZoom() {
        scaleFactor = 1.0f
        matrixScale.setScale(scaleFactor, scaleFactor)
        imageMatrix = matrixScale
    }
}