package com.example.android_galeria21

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnCaptureThumbnail: Button
    private lateinit var btnCaptureFullSize: Button
    private var currentPhotoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnCaptureThumbnail = findViewById(R.id.btnCaptureThumbnail)
        btnCaptureFullSize = findViewById(R.id.btnCaptureFullSize)

        btnSelectImage.setOnClickListener {
            checkAndRequestPermission()
        }

        btnCaptureThumbnail.setOnClickListener {
            captureThumbnailLauncher.launch(null)
        }

        btnCaptureFullSize.setOnClickListener {
            val photoFile = createImageFile()
            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationId}.fileprovider",
                    it
                )
                currentPhotoUri = photoURI
                captureFullSizeLauncher.launch(photoURI)
            }
        }
    }

    // Solicitar permiso
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(
                    this,
                    "Permiso denegado. No se puede acceder a la galería.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Abrir galería y recibir imagen
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    imageView.setImageURI(it)
                }
            }
        }

    private fun checkAndRequestPermission() {
        val permission = android.Manifest.permission.READ_MEDIA_IMAGES

        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        galleryLauncher.launch(intent)
    }

    // Launcher para capturar Thumbnail
    private val captureThumbnailLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                imageView.setImageBitmap(it)
            } ?: Toast.makeText(this, "No se pudo capturar el thumbnail", Toast.LENGTH_SHORT).show()
        }

    // Launcher para capturar Full-Size
    private val captureFullSizeLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                currentPhotoUri?.let {
                    imageView.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "No se pudo capturar la foto completa", Toast.LENGTH_SHORT).show()
            }
        }

    // Crear archivo temporal para foto completa
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}
