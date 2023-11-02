package com.example.tffapp.feature.saver

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.example.tffapp.common.data.ImageAnalysisResult
import com.example.tffapp.feature.render.ResultRenderer
import com.example.tffapp.util.ImageSaver
import org.tensorflow.lite.task.vision.detector.Detection
import java.text.SimpleDateFormat
import java.util.Date

class SaveHelper(
    context: Context, private var lastImageData: ImageAnalysisResult?
) {

    private val imageSaver: ImageSaver = ImageSaver(context)

    fun processSaveImageToGallery(bitmapBuffer: Bitmap) {
        var image = bitmapBuffer.copy(bitmapBuffer.config, true)
        lastImageData?.lastResults?.let {
            image = drawDetectionOnImage(it, image!!)
        }
        val fileName = getFileName()
        imageSaver.setFileName(fileName).save(image)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getFileName(): String {
        val sdf = SimpleDateFormat("dd-M-yyyy-hh-mm-ss")
        return sdf.format(Date()) + ".png"
    }

    private fun drawDetectionOnImage(detections: List<Detection>?, image: Bitmap): Bitmap {
        val iAR = image.width.toFloat() / image.height
        var lastImageW = lastImageData!!.lastImageW
        var lastImageH = lastImageData!!.lastImageH
        val liAR = lastImageW.toFloat() / lastImageH
        var outImage = image
        if (iAR != liAR) {
            // TODO improve orientation changes handling
            val temp = lastImageH
            lastImageH = lastImageW
            lastImageW = temp
            outImage = rotateBitmap(image, 90f)
        }
        val canvas = Canvas(outImage)
        val scaleFactor =
            java.lang.Float.max(image.width * 1f / lastImageW, image.height * 1f / lastImageH)
        ResultRenderer.render(canvas, detections!!, scaleFactor)
        return outImage
    }

    private fun rotateBitmap(
        original: Bitmap, degrees: Float
    ): Bitmap {
        val x = original.width
        val y = original.height
        val matrix = Matrix()
        matrix.preRotate(degrees)
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }


}