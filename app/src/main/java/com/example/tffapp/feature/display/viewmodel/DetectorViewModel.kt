package com.example.tffapp.feature.display.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tffapp.common.data.ImageAnalysisResult
import com.example.tffapp.feature.saver.SaveHelper
import com.example.tffapp.feature.tf.ObjectDetectorHelper
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DetectorViewModel(private val application: Application) : AndroidViewModel(application),
    ObjectDetectorHelper.DetectorListener {


    var useBackCamera: Boolean = true

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var lastImageData: ImageAnalysisResult? = null

    var imageAnalyzer: ImageAnalysis? = null

    private val _imageDataLiveData = MutableLiveData<Pair<Long, ImageAnalysisResult>>()
    val imageDataLiveData: LiveData<Pair<Long, ImageAnalysisResult>> = _imageDataLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    override fun onResults(
        results: MutableList<Detection>?, inferenceTime: Long, imageHeight: Int, imageWidth: Int
    ) {
        lastImageData = ImageAnalysisResult(
            results, imageWidth, imageHeight
        )
        _imageDataLiveData.postValue(Pair(inferenceTime, lastImageData!!))
    }

    override fun onError(error: String) {
        _errorLiveData.postValue(error)
    }

    private fun detectObjects(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

        val imageRotation = image.imageInfo.rotationDegrees
        // Pass Bitmap and rotation to the object detector helper for processing and detection
        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    fun shutDownExecutor() {
        cameraExecutor.shutdown()
    }

    fun onViewCreated() {
        // Initialize our background executor

        objectDetectorHelper = ObjectDetectorHelper(
            context = application, objectDetectorListener = this
        )

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun initImageAnalyzer(rotation: Int) {
        imageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor) { image ->
                    if (!::bitmapBuffer.isInitialized) {
                        // The image rotation and RGB image buffer are initialized only once
                        // the analyzer has started running
                        bitmapBuffer = Bitmap.createBitmap(
                            image.width, image.height, Bitmap.Config.ARGB_8888
                        )
                    }

                    detectObjects(image)
                }
            }
    }

    fun onConfigurationChanged(rotation: Int) {
        imageAnalyzer?.targetRotation = rotation
    }

    fun processSaveClick() {
        lastImageData?.let {
            SaveHelper(application, lastImageData).processSaveImageToGallery(bitmapBuffer)
        }
    }

    fun onCameraChangedCallback(checked: Boolean) {
        useBackCamera = !checked
    }

}