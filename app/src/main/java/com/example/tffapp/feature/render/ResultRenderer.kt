package com.example.tffapp.feature.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.Random


// TODO better as an interface impl
object ResultRenderer {
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var bounds = Rect()
    private val categoriesToColorMap = mutableMapOf<String, Int>()
    private const val BOUNDING_RECT_TEXT_PADDING = 8

    init {
        initPaints()
    }

    fun render(canvas: Canvas, results: List<Detection>, scaleFactor: Float) {
        for (result in results) {
            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            // Draw bounding box around detected objects
            boxPaint.color = getColorForCategory(result.categories[0].label)
            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)

            // Create text to display alongside detected objects
            val drawableText =
                result.categories[0].label + " " +
                        String.format("%.2f", result.categories[0].score)

            // Draw rect behind display text
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )

            // Draw text for detected object
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
        }
    }

    private fun getColorForCategory(label: String?): Int {
        if (label == null) {
            return Color.GREEN
        }
        return if (categoriesToColorMap.containsKey(label)) {
            categoriesToColorMap[label]!!
        } else {
            val nextColor = getRandomUniqueColor()
            categoriesToColorMap[label] = nextColor
            nextColor
        }
    }

    private fun getRandomUniqueColor(): Int {
        val rnd = Random()
        var color = getColor(rnd)
        while (categoriesToColorMap.containsValue(color)) {
            color = getColor(rnd)
        }
        return color
    }

    private fun getColor(rnd: Random): Int =
        Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))


    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = Color.GREEN
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }
}