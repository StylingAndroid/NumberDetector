package com.stylingandroid.numberdetector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class FingerCanvasView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : View(context, attrs, defStyleRes) {

    private val path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 150f
    }

    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private val tolerance = 4
    private val bitmapMinimumSize = 28f

    private var scaleFactor = 1f
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleFactor = bitmapMinimumSize / Math.min(w, h).toFloat()

        bitmap = Bitmap.createBitmap(
                Math.round(w.toFloat() * scaleFactor),
                Math.round(h.toFloat() * scaleFactor),
                Bitmap.Config.RGB_565
        )
        bitmapCanvas = Canvas(bitmap).apply {
            scale(scaleFactor, scaleFactor)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart(event.x, event.y)
            MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
            MotionEvent.ACTION_UP -> {
                touchEnd()
                performClick()
            }
        }
        return true
    }

    private fun touchStart(x: Float, y: Float) {
        path.moveTo(x, y)
        lastX = x
        lastY = y
        invalidate()
    }

    private fun touchMove(x: Float, y: Float) {
        val deltaX = Math.abs(x - lastX)
        val deltaY = Math.abs(y - lastY)
        if (deltaX >= tolerance || deltaY >= tolerance) {
            path.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f)
            lastX = x
            lastY = y
            invalidate()
        }
    }

    var drawingListener: (Bitmap) -> Unit = {}

    fun clear() {
        path.reset()
        invalidate()
    }

    private fun touchEnd() {
        path.lineTo(lastX, lastY)
    }

    override fun performClick(): Boolean {
        super.performClick()

        bitmapCanvas.drawColor(Color.WHITE)
        bitmapCanvas.drawPath(path, paint)
        drawingListener(bitmap)
        invalidate()
        return true
    }
}
