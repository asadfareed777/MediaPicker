package com.asad.mediapicker

import android.os.Environment
import java.io.File


object AppFolders {
    val THUMBS: String = "thumbs"
    val FILE_PATH_BASE: String = "/Android/data/com.asad.mediapicker/.files/"
    val IMAGE_FILE_PATH: String = FILE_PATH_BASE + ".images/"
    private val TEMP_IMAGE_FOLDER: String = "/.temp_image/"
    val VIDEO_DIRECTORY: String = "/pic_video_attachment"

    val imageFilePath: File
        get() = File(
            (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString()
                    + IMAGE_FILE_PATH)
        )

    val randomImageFilePath: String
        get() {
            return ((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString()
                    + IMAGE_FILE_PATH
                    + (System.currentTimeMillis() / 1000) + ".jpg"))
        }

    val imagePath: String
        get() {
            return (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString()
                    + IMAGE_FILE_PATH)
        }


    val userThumbnailPath: String
        get() {
            return ((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + FILE_PATH_BASE + THUMBS))
        }

    val videoFilePath: File
        get() {
            return File(
                (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString()
                        + VIDEO_DIRECTORY)
            )
        }

    val randomVideoFilePath: String
        get() {
            return ((Environment.getExternalStorageDirectory().toString()
                    + VIDEO_DIRECTORY
                    + (System.currentTimeMillis() / 1000) + ".mp4"))
        }
}