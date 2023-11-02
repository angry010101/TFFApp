package com.example.tffapp.feature.display

import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import com.example.tffapp.R
import com.example.tffapp.feature.tf.ObjectDetectorHelper
import com.example.tffapp.feature.tf.OverlayView

class MainUIHelper(
    private val layout: View,
    private val objectDetectorHelper: ObjectDetectorHelper
) {

    private val context = layout.context

    var cameraChangedCallback: (isChecked: Boolean) -> Unit = {}

    fun initBottomSheetControls() {
        // When clicked, lower detection score threshold floor
        val threshold = layout.findViewById<SeekBar>(R.id.threshold_seekbar)

        val maxResultsMinus = layout.findViewById<View>(R.id.max_results_minus)
        val maxResultsPlus = layout.findViewById<View>(R.id.max_results_plus)

        val threadsMinus = layout.findViewById<View>(R.id.threads_minus)
        val threadsPlus = layout.findViewById<View>(R.id.threads_plus)

        val spinnerDelegate = layout.findViewById<AppCompatSpinner>(R.id.spinner_delegate)
        val spinnerModel = layout.findViewById<AppCompatSpinner>(R.id.spinner_model)

        val useBackCameraSwitch = layout.findViewById<SwitchCompat>(R.id.camera_switch)

        useBackCameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            cameraChangedCallback(isChecked)
        }

        threshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val sb = seekBar!!
                val max = sb.max
                val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sb.min
                } else {
                    0
                }
                val value = (progress - min).toFloat() / (max - min)
                // 0..1
                objectDetectorHelper.threshold = value
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateControlsUi()
            }
        })

        // When clicked, reduce the number of objects that can be detected at a time
        maxResultsMinus.setOnClickListener {
            if (objectDetectorHelper.maxResults > 1) {
                objectDetectorHelper.maxResults--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of objects that can be detected at a time
        maxResultsPlus.setOnClickListener {
            if (objectDetectorHelper.maxResults < 5) {
                objectDetectorHelper.maxResults++
                updateControlsUi()
            }
        }

        // When clicked, decrease the number of threads used for detection
        threadsMinus.setOnClickListener {
            if (objectDetectorHelper.numThreads > 1) {
                objectDetectorHelper.numThreads--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of threads used for detection
        threadsPlus.setOnClickListener {
            if (objectDetectorHelper.numThreads < 4) {
                objectDetectorHelper.numThreads++
                updateControlsUi()
            }
        }

        // When clicked, change the underlying hardware used for inference. Current options are CPU
        // GPU, and NNAPI
        spinnerDelegate.setSelection(0, false)
        spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentDelegate = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }

        // When clicked, change the underlying model used for object detection
        spinnerModel.setSelection(0, false)
        spinnerModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentModel = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }

        updateThresholdValue()
    }


    private fun updateThresholdValue() {
        val thresholdValue = layout.findViewById<TextView>(R.id.threshold_value)
        thresholdValue.text =
            context?.getString(
                R.string.label_confidence_threshold,
                String.format("%.2f", objectDetectorHelper.threshold)
            )
    }


    // Update the values displayed in the bottom sheet. Reset detector.
    private fun updateControlsUi() {
        val maxResultsValue = layout.findViewById<TextView>(R.id.max_results_value)
        val threadsValue = layout.findViewById<TextView>(R.id.threads_value)

        maxResultsValue.text =
            objectDetectorHelper.maxResults.toString()

        updateThresholdValue()

        threadsValue.text =
            objectDetectorHelper.numThreads.toString()

        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        objectDetectorHelper.clearObjectDetector()
        layout.findViewById<OverlayView>(R.id.overlay).invalidate()
    }

}