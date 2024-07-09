package com.asad.mediapicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity(), ImagePickerUtility.ImagePickerListener{

    var imagePickerUtility: ImagePickerUtility? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imagePickerUtility = ImagePickerUtility(
            this, null,
            this, ImagePickerUtility.MODE_TAKE_IMAGE_BOTH_CAMERA_GALLERY
        )
        // fetch image you can use click listeners or other logic
        imagePickerUtility!!.methodRequiresPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePickerUtility!!.onActivityResult(this, requestCode, resultCode, data)
//        imagePickerUtility!!.methodRequiresPermission()
    }


    override fun onImageRequestCompleted(filePath: String?, resultCode: Int, imageUri: Uri?) {
        runOnUiThread {
            if (filePath != null) {
                val file = File(filePath)
                val file_size = (file.length() / 1024).toString().toInt()
                Log.wtf("fileSize", file_size.toString())
                Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show()
            }
        }
    }
}