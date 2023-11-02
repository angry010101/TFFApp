package com.example.tffapp.common.data

import org.tensorflow.lite.task.vision.detector.Detection

data class ImageAnalysisResult(
    val lastResults: List<Detection>? = null,
    val lastImageW: Int,
    val lastImageH: Int
)