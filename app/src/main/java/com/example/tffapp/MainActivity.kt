package com.example.tffapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tffapp.common.data.IDisplayObjectsCount
import com.example.tffapp.common.data.ISavePictureButton

class MainActivity : AppCompatActivity(), IDisplayObjectsCount, ISavePictureButton {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun getObjectCountView(): TextView =
        findViewById(R.id.objects_count)

    override fun getSaveButton(): Button =
        findViewById(R.id.save_image_button)
}