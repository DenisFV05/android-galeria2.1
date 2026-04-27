package com.example.android_galeria21

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnCaptureThumbnail: Button
    private lateinit var btnCaptureFullSize: Button

    private var currentPhotoUri: Uri? = null

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageView.setImageURI(it)
            }
        }

    private val captureThumbnailLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "No se pudo capturar el thumbnail", Toast.LENGTH_SHORT).show()
            }
        }

    private val captureFullSizeLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                currentPhotoUri?.let { uri ->
                    imageView.setImageURI(uri)
                }
            } else {
                currentPhotoUri?.let { uri ->
                    contentResolver.delete(uri, null, null)
                }
                Toast.makeText(this, "No se pudo guardar la foto completa", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnCaptureThumbnail = findViewById(R.id.btnCaptureThumbnail)
        btnCaptureFullSize = findViewById(R.id.btnCaptureFullSize)

        btnSelectImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnCaptureThumbnail.setOnClickListener {
            captureThumbnailLauncher.launch(null)
        }

        btnCaptureFullSize.setOnClickListener {
            val photoUri = createImageUri()
            currentPhotoUri = photoUri
            captureFullSizeLauncher.launch(photoUri)
        }
    }

    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/AndroidGaleria21"
                )
            }
        }

        return contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IllegalStateException("No se pudo crear la imagen en MediaStore")
    }
}