package com.example.tffapp.feature.display

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tffapp.R
import com.example.tffapp.common.data.IDisplayObjectsCount
import com.example.tffapp.common.data.ISavePictureButton
import com.example.tffapp.common.data.ImageAnalysisResult
import com.example.tffapp.feature.display.viewmodel.DetectorViewModel
import com.example.tffapp.feature.tf.OverlayView
import com.example.tffapp.util.PermissionUtil
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.LinkedList

class CameraFragment : Fragment() {

    private val TAG = "ObjectDetection"

    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var layout: View
    private lateinit var uiHelper: MainUIHelper

    private val viewModel: DetectorViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        PermissionUtil.checkPermission(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Shut down our background executor
        viewModel.shutDownExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        layout = inflater.inflate(R.layout.fragment_camera, container, false)
        return layout
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.onConfigurationChanged(getViewFinder().display.rotation)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.onViewCreated()
        uiHelper = MainUIHelper(layout, viewModel.objectDetectorHelper)
        uiHelper.cameraChangedCallback = { isChecked ->
            viewModel.onCameraChangedCallback(isChecked)
            setUpCamera()
        }

        // Wait for the views to be properly laid out
        layout.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Attach listeners to UI control widgets
        uiHelper.initBottomSheetControls()
        initSaveImageToGalleryBtn()
        updateObjectsCount(0)
        initLiveData()
    }

    private fun updateObjectsCount(c: Int) {
        if (requireActivity() is IDisplayObjectsCount) {
            (requireActivity() as IDisplayObjectsCount).getObjectCountView().text =
                context?.getString(R.string.detected_objects_count, c)
        }
    }

    private fun initLiveData() {
        viewModel.errorLiveData.observe(viewLifecycleOwner, ::onError)
        viewModel.imageDataLiveData.observe(viewLifecycleOwner) {
            onResults(it.second, it.first)
        }
    }

    private fun initSaveImageToGalleryBtn() {
        if (requireActivity() is ISavePictureButton) {
            (requireActivity() as ISavePictureButton).getSaveButton().setOnClickListener {
                viewModel.processSaveClick()
            }
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector = CameraSelector.Builder().requireLensFacing(
            if (viewModel.useBackCamera) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
        ).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(getViewFinder().display.rotation).build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        viewModel.initImageAnalyzer(getViewFinder().display.rotation)

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, viewModel.imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(getViewFinder().surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun getViewFinder() = layout.findViewById<PreviewView>(R.id.view_finder)

    private fun getOverlay() = layout.findViewById<OverlayView>(R.id.overlay)

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    private fun onResults(
        imageAnalysisResult: ImageAnalysisResult, inferenceTime: Long
    ) {
        activity?.runOnUiThread {
            getOverlay().apply {
                layout.findViewById<TextView>(R.id.inference_time_val).text =
                    String.format("%d ms", inferenceTime)
                with(imageAnalysisResult) {
                    setResults(
                        lastResults ?: LinkedList<Detection>(), lastImageH, lastImageW
                    )
                    updateObjectsCount(lastResults?.size ?: 0)
                }
                // Force a redraw
                invalidate()
            }
        }
    }

    private fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
}