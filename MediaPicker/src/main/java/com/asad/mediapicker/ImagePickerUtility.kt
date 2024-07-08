package com.asad.mediapicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


const val ASK_PERMISSIONS:Int = 10

class ImagePickerUtility : View.OnClickListener, PermissionCallbacks {
    //Permission
    private var mimeType: String? = null
    private var imageCaptureUri: Uri? = null
    private var modeType = 1
    private var mContext: Activity? = null
    private var mFragmentContext: Fragment? = null
    private var request: CameraRequest?
    private val baseFolderPath: String? = null
    private var imageSourcePath: String? = null
    private var imageDestinationPath: String? = null
    private var imagePickerListiner: ImagePickerListener?
    private val PERMISSIONS_CAMERA = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val permissions13 = arrayOf(
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA
    )
    private var permissions: Array<String>? = null
    private var dialog: AlertDialog? = null
    private var mLlCameraPicture: LinearLayout? = null
    private val mLlCameraVideo: LinearLayout? = null
    private var mLlFilePicture: LinearLayout? = null
    private val mLlFileVideo: LinearLayout? = null
    private var mTextView: TextView? = null
    private var saveVideoToSDCard = false
    private var alertDialog: AlertDialog? = null

    var backgroundService: ExecutorService = Executors.newSingleThreadExecutor()


    /**
     * @param context
     * @param cameraRequest
     * @param listener
     * @param modeType
     * @param saveVideoToSDcard
     */
    constructor(
        context: AppCompatActivity?,
        cameraRequest: CameraRequest?,
        listener: ImagePickerListener?,
        modeType: Int,
        saveVideoToSDcard: Boolean
    ) {
        this.request = cameraRequest
        this.modeType = modeType
        this.mContext = context
        this.imagePickerListiner = listener
        this.saveVideoToSDCard = saveVideoToSDcard
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions13
        } else {
            PERMISSIONS_CAMERA
        }
        createDirectory()

        if (this.request != null) {
            userActionRequest(cameraRequest)
        }
    }

    /***
     *
     * @param activityContext
     * @param fragmentContext
     * @param cameraRequest
     * @param listener
     * @param modeType
     * @param saveVideoToSDcard
     */
    constructor(
        activityContext: Activity?,
        fragmentContext: Fragment?,
        cameraRequest: CameraRequest?,
        listener: ImagePickerListener?,
        modeType: Int,
        saveVideoToSDcard: Boolean
    ) {
        this.request = cameraRequest
        this.modeType = modeType
        this.mContext = activityContext
        this.imagePickerListiner = listener
        this.mFragmentContext = fragmentContext
        this.saveVideoToSDCard = saveVideoToSDcard

        createDirectory()

        if (this.request != null) {
            userActionRequest(cameraRequest)
        }
    }


    private fun checkFileSize(path: String): Long {
        val file = File(path)
        return file.length()
    }

    /***
     *
     * @param context
     * @param cameraRequest
     * @param listener
     * @param modeType
     */
    constructor(
        context: AppCompatActivity?,
        cameraRequest: CameraRequest?,
        listener: ImagePickerListener?,
        modeType: Int
    ) {
        this.request = cameraRequest
        this.modeType = modeType
        this.mContext = context
        this.imagePickerListiner = listener

        createDirectory()

        if (this.request != null) {
            userActionRequest(cameraRequest)
        }
    }

    /***
     *
     * @param activityContext
     * @param fragmentContext
     * @param cameraRequest
     * @param listener
     * @param modeType
     */
    constructor(
        activityContext: Activity?,
        fragmentContext: Fragment?,
        cameraRequest: CameraRequest?,
        listener: ImagePickerListener?,
        modeType: Int
    ) {
        this.request = cameraRequest
        this.modeType = modeType
        this.mContext = activityContext
        this.imagePickerListiner = listener
        this.mFragmentContext = fragmentContext

        createDirectory()

        if (this.request != null) {
            userActionRequest(cameraRequest)
        }
    }

    fun setModeType(modeType: Int) {
        this.modeType = modeType
    }

    private fun showFileChooser() {
        val intent = Intent()
        //sets the select file to all types of files
        intent.setType("*/*")
        intent.putExtra("CONTENT_TYPE", "*/*")
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT)
        //starts new activity to select file and return data
        //  mContext.startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);
        if (mFragmentContext != null) {
            startChildActivityFragment(
                Intent.createChooser(intent, "Choose File to Upload.."),
                MODE_CHOOSE_ONLY_ANY_FILE
            )
        } else {
            startChildActivity(
                Intent.createChooser(intent, "Choose File to Upload.."),
                MODE_CHOOSE_ONLY_ANY_FILE
            )
        }
    }

    fun userActionRequest(request: CameraRequest?) {
        when (request) {
            CameraRequest.CAPTURE_IMAGE_REQUEST -> captureImageFromCamera()
            CameraRequest.CHOOSE_IMAGE_REQUEST -> imageFromGallery
            CameraRequest.CHOOSE_ANY_FILE -> showFileChooser()
            CameraRequest.RECORD_VIDEO_REQUEST -> takeVideoFromCamera()
            CameraRequest.CHOOSE_VIDEO_REQUEST -> chooseVideoFromGallery()
            else -> {}
        }
    }

    private fun startChildActivity(intent: Intent, result: Int) {
        try {
            mContext!!.startActivityForResult(intent, result)
        } catch (e: Exception) {
            val f = File(Environment.getExternalStorageDirectory(), "temp.jpg")
            //Intent intentNew = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    mContext!!, mContext!!.packageName + ".provider",
                    randomFileName
                )
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            mContext!!.startActivityForResult(intent, result)
        }
    }

    private fun startChildActivityFragment(intent: Intent, result: Int) {
        try {
            mFragmentContext!!.startActivityForResult(intent, result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun captureImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val path: File = AppFolders.imageFilePath
        if (!path.exists()) {
            path.mkdirs()
        }
        val file: File = File(AppFolders.randomImageFilePath)
        imageCaptureUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                mContext!!, mContext!!.getPackageName() + ".provider",
                randomFileName
            )
        } else {
            Uri.fromFile(file)
        }
        intent.putExtra("return-data", true)
        intent.putExtra(
            MediaStore.EXTRA_OUTPUT,
            imageCaptureUri
        )
        if (mFragmentContext != null) {
            startChildActivityFragment(intent, PIC_FROM_CAMERA)
        } else {
            startChildActivity(intent, PIC_FROM_CAMERA)
        }
    }

    private val randomFileName: File
        get() = File(AppFolders.imagePath, imageFileName)

    private val imageFromGallery: Unit
        get() {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setType("image/*")
            val pm = mContext!!.packageManager
            val activityList = pm.queryIntentActivities(intent, 0)
            val len = activityList.size
            for (i in 0 until len) {
                val app = activityList[i] as ResolveInfo
                val activity = app.activityInfo
                if (activity.packageName.contains("com.google.android.gallery")
                    || activity.packageName.contains("com.htc.album")
                    || activity.packageName.contains("android.gallery3d")
                ) {
                    intent.setClassName(activity.packageName, activity.name)
                    break
                }
            }
            if (mFragmentContext != null) {
                startChildActivityFragment(intent, PIC_FROM_FILE)
            } else {
                startChildActivity(intent, PIC_FROM_FILE)
            }
        }

    fun getMimeType(uri: Uri?): String? {
        mimeType = mContext!!.contentResolver.getType(uri!!)
        if (mimeType!!.contains("image/")) {
            mimeType = MIMETYPE_IMAGE
        } else if (mimeType!!.contains("video/")) {
            mimeType = MIMETYPE_VIDEO
        } else if (mimeType!!.contains("application/")) {
            mimeType = MIMETYPE_DOC
        }
        return mimeType
    }

    fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == PIC_FROM_FILE || requestCode == PIC_FROM_CAMERA || requestCode == ONLY_FILE) {
                    if (requestCode == PIC_FROM_FILE) {
                        checkImageUri(data)
                    } else if (requestCode == PIC_FROM_CAMERA) {
                        imageSourcePath = imageCaptureUri!!.path
                    } else if (requestCode == ONLY_FILE) {
                        if (data != null) {
                            val selectedFileUri = data.data
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                sendDataToParent(
                                    FileUtils.getPath(mContext, selectedFileUri!!),
                                    requestCode
                                )
                            }
                        }
                    }
                    val myFile = File(imageSourcePath)
                    val file_size = (myFile.length() / 1024).toString().toInt()
                    Log.wtf("fileSize", file_size.toString())
                    val maxSize = 1024 * 2
                    //                    if (file_size < maxSize){
                    if (imageSourcePath != null && imageCaptureUri != null) {
                        resizeAndCompressBitmap(context, requestCode)
                    }
                    //                    }else {
//                        Toast.makeText(mContext, "File size must be less than 2 MB",Toast.LENGTH_LONG).show();
//                    }
                } else if (requestCode == VIDEO_GALLERY || requestCode == VIDEO_CAMERA) {
                    if (requestCode == VIDEO_GALLERY) {
                        Log.d("GALLERY VIDEO", "gallery")
                        if (data != null) {
                            val contentURI = data.data

                            val selectedVideoPath = getPath(contentURI)
                            Log.d("path", selectedVideoPath!!)


                            //Toast.makeText(mContext, selectedVideoPath, Toast.LENGTH_SHORT).show();
                            saveVideoToMemory(selectedVideoPath, requestCode)

                            sendVideoDataToParent(selectedVideoPath, requestCode, contentURI)
                        }
                    } else if (requestCode == VIDEO_CAMERA) {
                        val contentURI = data!!.data
                        val recordedVideoPath = getPath(contentURI)

                        Log.d("CAMERA VIDEO", recordedVideoPath!!)

                        //Toast.makeText(mContext, recordedVideoPath, Toast.LENGTH_SHORT).show();
                        saveVideoToMemory(recordedVideoPath, requestCode)

                        sendVideoDataToParent(recordedVideoPath, requestCode, contentURI)
                    }
                }
            }
        } catch (exception: NullPointerException) {
        }
    }

    private fun sendVideoDataToParent(path: String?, requestCode: Int, videoUri: Uri?) {
        imageSourcePath = path
        imageCaptureUri = videoUri
        if (imagePickerListiner != null) {
            imagePickerListiner!!.onImageRequestCompleted(path, requestCode, videoUri)
        }
    }

    val imageFileName: String
        get() = "image_" + (System.currentTimeMillis() / 1000).toString() + ".jpg"

    private fun createDirectory() {
        if (baseFolderPath != null) {
            val file: File = File(AppFolders.userThumbnailPath, "")
            file.mkdirs()
        }
    }

    @Suppress("deprecation")
    private fun getRealPathFromURI(path: String): String {
        val cursor: Cursor
        if (path.contains("content://")) {
            val contentUri = Uri.parse(path)

            val proj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA)
            cursor = mContext!!.managedQuery(contentUri, proj, null, null, null)

            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else {
            return path
        }
    }

    private fun sendDataToParent(path: String?, requestCode: Int, imageUri: Uri?) {
        imageSourcePath = path
        imageCaptureUri = imageUri
        if (imagePickerListiner != null) {
            imagePickerListiner!!.onImageRequestCompleted(path, requestCode, imageUri)
        }
    }

    private fun sendDataToParent(path: String, requestCode: Int) {
        imageSourcePath = path
        if (imagePickerListiner != null) {
            imagePickerListiner!!.onImageRequestCompleted(path, requestCode, null)
        }
    }

    private fun checkImageUri(data: Intent?) {
        val mImageCaptureUri = data!!.data
        val mImagePath = getRealPathFromURI(mImageCaptureUri.toString())
        if (mImagePath != null) {
            this.imageCaptureUri = mImageCaptureUri
            this.imageSourcePath = mImagePath
        } else {
            this.imageCaptureUri = null
            this.imageSourcePath = null
        }
    }

    private fun resizeAndCompressBitmap(context: Context, requestCode: Int) {
        try {
            var bmp = compressImage(imageSourcePath)

            imageDestinationPath = imageSourcePath
            if (requestCode == PIC_FROM_FILE) {
                imageDestinationPath =
                    AppFolders.randomImageFilePath //getRandomeFileName().getAbsolutePath();
            }
            val path: File = AppFolders.imageFilePath
            if (!path.exists()) {
                path.mkdirs()
            }
            val file = File(imageDestinationPath)
            if (!file.exists()) {
                file.createNewFile()
            }

            bmp = getBitmapWithTimeStamp(context, bmp)
            var fos: FileOutputStream? = null

            fos = FileOutputStream(file)
            if (fos != null) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 65, fos)
                fos.close()
            }
            // rotatedBitmap.recycle();
            bmp.recycle()
            sendDataToParent(imageDestinationPath, requestCode, imageCaptureUri)
        } catch (exception: IOException) {
            DebugHelper.print("ImagePickerUtility", exception, true)
        } catch (exception: NullPointerException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    fun compressImage(imagePath: String?): Bitmap {
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
        val maxRatio = maxWidth / maxHeight
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        options.inJustDecodeBounds = false
        options.inDither = false
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        val bmp = BitmapFactory.decodeFile(imagePath, options)

        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp!!,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        if (bmp != null) {
            bmp.recycle()
        }
        // try {
        val exif = ExifInterface(imagePath!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
        val matrix = Matrix()
        if (orientation == 6) {
            matrix.postRotate(90f)
        } else if (orientation == 3) {
            matrix.postRotate(180f)
        } else if (orientation == 8) {
            matrix.postRotate(270f)
        }
        scaledBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )

        return scaledBitmap
    }

    fun getBitmapWithTimeStamp(context: Context?, bitmap: Bitmap?): Bitmap {
        val workingBitmap = Bitmap.createBitmap(bitmap!!)
        val sdf = SimpleDateFormat("dd-MM-yy  HH:mm a")
        val dateTime = sdf.format(Calendar.getInstance().time)
        val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val cs = Canvas(mutableBitmap)
        val tPaint = Paint()
        tPaint.textSize = 30f
        tPaint.color = Color.RED
        tPaint.style = Paint.Style.FILL
        cs.drawText(
            dateTime,
            (mutableBitmap.width - 300).toFloat(),
            (mutableBitmap.height - 20).toFloat(),
            tPaint
        )
        return mutableBitmap
    }

    //Permissions
    private fun showSelectPicture() {
        val dialogBuilder = AlertDialog.Builder(
            mContext!!
        )
        val inflater = mContext!!.layoutInflater

        val dialogView: View = inflater.inflate(R.layout.picture_dialog_file, null)
        dialogBuilder.setView(dialogView)

        mLlCameraPicture = dialogView.findViewById<LinearLayout>(R.id.ll_camera)
        mLlFilePicture = dialogView.findViewById<LinearLayout>(R.id.ll_file)
        mTextView = dialogView.findViewById<TextView>(R.id.text)

        if (modeType == MODE_TAKE_IMAGE_BOTH_CAMERA_GALLERY) {
            mTextView!!.setText("Choose Images From Gallery")
        } else if (modeType == MODE_TAKE_PIC_OR_CHOOSE_ANY_FILE) {
            mTextView!!.setText("Choose File")
        }

        mLlCameraPicture!!.setOnClickListener(this)
        mLlFilePicture!!.setOnClickListener(this)
        dialog = dialogBuilder.show()
    }


    fun getFileSize(filePath: String?): Long {
        val file = File(filePath)

        // Get length of file in bytes
        val fileSizeInBytes = file.length()
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = fileSizeInBytes / 1024
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        val fileSizeInMB = fileSizeInKB / 1024

        if (fileSizeInMB > 27) {
        }
        return fileSizeInMB
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ll_camera -> {
                if (dialog != null) {
                    dialog!!.cancel()
                }
                userActionRequest(CameraRequest.CAPTURE_IMAGE_REQUEST)
            }

            R.id.ll_file -> {
                if (dialog != null) {
                    dialog!!.cancel()
                }
                if (modeType == MODE_TAKE_PIC_OR_CHOOSE_ANY_FILE) {
                    showFileChooser()
                } else {
                    userActionRequest(CameraRequest.CHOOSE_IMAGE_REQUEST)
                }
            }

            R.id.ll_camera_video -> {
                if (dialog != null) {
                    dialog!!.cancel()
                }
                userActionRequest(CameraRequest.RECORD_VIDEO_REQUEST)
            }

            R.id.ll_file_video -> {
                if (dialog != null) {
                    dialog!!.cancel()
                }
                chooseVideoFromGallery()
            }

            else -> {}
        }
    }

    //Permissions
    @AfterPermissionGranted(ASK_PERMISSIONS)
    fun methodRequiresPermission() {
        if (EasyPermissions.hasPermissions(mContext!!, *permissions!!)) {
            when (modeType) {
                MODE_TAKE_IMAGE_CAMERA_ONLY -> userActionRequest(CameraRequest.CAPTURE_IMAGE_REQUEST)
                MODE_TAKE_IMAGE_BOTH_CAMERA_GALLERY -> showSelectPicture()
                MODE_TAKE_VIDEO_BOTH_RECORD_GALLERY -> showSelectVideo()
                MODE_TAKE_IMAGE_GALLERY_ONLY -> userActionRequest(CameraRequest.CHOOSE_IMAGE_REQUEST)
                MODE_TAKE_PIC_OR_CHOOSE_ANY_FILE -> showSelectPicture()
                MODE_CHOOSE_ONLY_ANY_FILE -> showFileChooser()
                MODE_RECORD_VIDEO_ONLY -> takeVideoFromCamera()
                MODE_GALLERY_VIDEO_ONLY -> chooseVideoFromGallery()
            }
        } else {
            EasyPermissions.requestPermissions(
                mContext!!,
                "Application requires following permissions to work properly",
                ASK_PERMISSIONS,
                *permissions!!
            )
            //            EasyPermissions.requestPermissions(
//                    new PermissionRequest.Builder(mContext, ASK_PERMISSIONS, PERMISSIONS_CAMERA)
//                            .setRationale("Application requires following permissions to work properly")
//                            .setPositiveButtonText("Ok")
//                            .setNegativeButtonText("Cancel")
//                            .build());
        }
    }

    private fun showSelectVideo() {
        val dialogBuilder = AlertDialog.Builder(
            mContext!!
        )
        val inflater = mContext!!.layoutInflater

        val dialogView: View = inflater.inflate(R.layout.video_dialog_file, null)
        dialogBuilder.setView(dialogView)

        val mLlCameraVideo = dialogView.findViewById<LinearLayout>(R.id.ll_camera_video)
        val mLlFileVideo = dialogView.findViewById<LinearLayout>(R.id.ll_file_video)
        mTextView = dialogView.findViewById<TextView>(R.id.text)

        if (modeType == MODE_TAKE_VIDEO_BOTH_RECORD_GALLERY) {
            mTextView!!.setText("Choose Video From Gallery")
        }


        mLlCameraVideo.setOnClickListener(this)
        mLlFileVideo.setOnClickListener(this)
        dialog = dialogBuilder.show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        var permission = ""
        if (perms != null) {
            for (i in perms.indices) {
                permission += perms[i]
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        var permissionsName = ""
        var permission = ""
        if (perms != null) {
            for (i in perms.indices) {
                permission += perms[i]
                permissionsName += perms[i] + "\n"
            }
        }

        /*new AppSettingsDialog.Builder(mContext, mContext.getString(R.string.rationale_ask_again))
//                .setRationale(mContext.getString(R.string.rationale_ask_again) + "\n" + permissionsName)
                .setTitle(mContext.getString(R.string.title_settings_dialog))
                .setPositiveButton("Settings")
//                .setNegativeButton("Cancel")
                .setRequestCode(ASK_PERMISSIONS)
                .build()
                .show();*/
        val builder = AlertDialog.Builder(
            mContext!!
        )
        builder.setView(R.layout.view_dialog)
        builder.setMessage(mContext!!.getString(R.string.rationale_ask_again))
            .setCancelable(false)
            .setTitle(mContext!!.getString(R.string.title_settings_dialog))
            .setPositiveButton("Yes") { dialog, id -> /*mContext.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));*/
                dialog.dismiss()
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", mContext!!.packageName, null)
                intent.setData(uri)
                mContext!!.startActivity(intent)
            }
            .setNegativeButton("No") { dialog, id -> // mContext.finish();
                dialog.dismiss()
            }
        alertDialog = builder.create()
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog!!.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (mFragmentContext != null) {
            mFragmentContext!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mContext!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    fun chooseVideoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        if (mFragmentContext != null) {
            mFragmentContext!!.startActivityForResult(galleryIntent, VIDEO_GALLERY)
        } else {
            mContext!!.startActivityForResult(galleryIntent, VIDEO_GALLERY)
        }
    }

    // video
    private fun takeVideoFromCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (mFragmentContext != null) {
            mFragmentContext!!.startActivityForResult(intent, VIDEO_CAMERA)
        } else {
            mContext!!.startActivityForResult(intent, VIDEO_CAMERA)
        }
    }

    private fun saveVideoToMemory(filePath: String?, requestCode: Int) {
        if (saveVideoToSDCard && hasRealRemovableSdCard(mContext)) {
            saveVideoToExternalStorage(filePath)
        } else {
            saveVideoToInternalStorage(requestCode)
        }
    }

    private fun saveVideoToInternalStorage(requestCode: Int) {
        try {
            var bmp = compressImage(imageSourcePath)

            imageDestinationPath = imageSourcePath
            val file = File(imageDestinationPath)
            if (!file.exists()) {
                file.createNewFile()
            }

            bmp = getBitmapWithTimeStamp(mContext, bmp)
            var fos: FileOutputStream? = null

            fos = FileOutputStream(file)
            if (fos != null) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                fos.close()
            }
            // rotatedBitmap.recycle();
            bmp.recycle()
            sendVideoDataToParent(imageDestinationPath, requestCode, imageCaptureUri)
        } catch (exception: IOException) {
            DebugHelper.print("ImagePickerUtility", exception, true)
        } catch (exception: NullPointerException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveVideoToExternalStorage(filePath: String?) {
        val newFile: File
        try {
            val currentFile = File(filePath)
            val wallpaperDirectory =
                File(Environment.getExternalStorageDirectory().toString() + VIDEO_DIRECTORY)
            newFile =
                File(wallpaperDirectory, Calendar.getInstance().timeInMillis.toString() + ".mp4")

            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs()
            }

            if (currentFile.exists()) {
                val `in`: InputStream = FileInputStream(currentFile)
                val out: OutputStream = FileOutputStream(newFile)

                val buf = ByteArray(1024)
                var len: Int

                while ((`in`.read(buf).also { len = it }) > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
                Log.v("vii", "Video file saved successfully.")
            } else {
                Log.v("vii", "Video saving failed. Source file missing.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = mContext!!.contentResolver.query(uri!!, projection, null, null, null)
        if (cursor != null) {
            // HERE YOU WILL GET A NULL POINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else return null
    }

    enum class CameraRequest {
        CAPTURE_IMAGE_REQUEST, CHOOSE_IMAGE_REQUEST, CHOOSE_ANY_FILE, RECORD_VIDEO_REQUEST, CHOOSE_VIDEO_REQUEST;

        companion object {
            fun getRequest(ordianl: Int): CameraRequest {
                return CameraRequest.values()
                    .get(ordianl)
            }
        }
    }

    interface ImagePickerListener {
        fun onImageRequestCompleted(filePath: String?, resultCode: Int, imageUri: Uri?)
    }

    companion object {
        const val MODE_TAKE_IMAGE_CAMERA_ONLY: Int = 1
        const val MODE_TAKE_IMAGE_BOTH_CAMERA_GALLERY: Int = 2
        const val MODE_CHOOSE_ONLY_ANY_FILE: Int = 3
        const val MODE_TAKE_PIC_OR_CHOOSE_ANY_FILE: Int = 4
        const val MODE_TAKE_VIDEO_BOTH_RECORD_GALLERY: Int = 5
        const val MODE_RECORD_VIDEO_ONLY: Int = 6
        const val MODE_GALLERY_VIDEO_ONLY: Int = 7

        const val PIC_FROM_CAMERA: Int = 1
        const val PIC_FROM_FILE: Int = 2
        const val ONLY_FILE: Int = 3
        const val VIDEO_GALLERY: Int = 4
        const val VIDEO_CAMERA: Int = 5
        const val MODE_TAKE_IMAGE_GALLERY_ONLY: Int = 8

        private const val maxHeight = 1280.0f
        private const val maxWidth = 1280.0f
        private const val VIDEO_DIRECTORY = "/pic_video_attachment"
        var MIMETYPE_IMAGE: String = "Images"
        var MIMETYPE_VIDEO: String = "Video"
        var MIMETYPE_DOC: String = "Documents"

        fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }
            val totalPixels = (width * height).toFloat()
            val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
            return inSampleSize
        }

        fun adjustImageOrientation(filePath: String?): Bitmap {
            val options = BitmapFactory.Options()
            options.inSampleSize = 2

            val imageBitmaps = BitmapFactory.decodeFile(filePath, options)

            var finalImage = imageBitmaps
            val exif: ExifInterface
            try {
                exif = ExifInterface(filePath!!)
                val exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                var rotate = 0
                when (exifOrientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                }
                if (rotate != 0) {
                    val w = imageBitmaps.width
                    val h = imageBitmaps.height

                    // Setting pre rotate
                    val mtx = Matrix()
                    mtx.preRotate(rotate.toFloat())

                    // Rotating Bitmap & convert to ARGB_8888, required by tess
                    finalImage = Bitmap.createBitmap(imageBitmaps, 0, 0, w, h, mtx, false)
                    imageBitmaps.recycle()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return finalImage
        }

        fun convertToBase64(imagePath: String?): String {
            var inputStream: InputStream? = null
            var encodedFile = ""
            try {
                inputStream = FileInputStream(imagePath)

                val buffer = ByteArray(10240) //specify the size to allow
                var bytesRead: Int
                val output = ByteArrayOutputStream()
                val output64 = Base64OutputStream(output, Base64.DEFAULT)

                while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                    output64.write(buffer, 0, bytesRead)
                }
                output64.close()
                try {
                    encodedFile = output.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val lastVal = encodedFile
            return lastVal
        }

        fun hasRealRemovableSdCard(context: Context?): Boolean {
            return ContextCompat.getExternalFilesDirs(context!!, null).size >= 2
        }
    }
}