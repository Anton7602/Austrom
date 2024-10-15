package com.colleagues.austrom.dialogs

import android.Manifest
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.R
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Transaction
import java.io.File

class ImageSelectionDialogFragment(private val transaction: Transaction, private val receiver: IDialogInitiator) : DialogFragment() {
    private lateinit var makePhoto: Button
    private lateinit var choosePhoto: Button
    private lateinit var imageHolder: ImageView
    private lateinit var imageUri: Uri
    private val cameraRequestCode = 100
    private val galleryRequestCode = 101
    private val requestPermissionsCustom = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }
    private val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            copyImageFromUri(uri)
            imageHolder.setImageURI(uri)
            receiver.receiveValue("true", "ImageUpdate")
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            imageHolder.setImageURI(imageUri)
            receiver.receiveValue("true", "ImageUpdate")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_image_selection, null)
        bindViews(view)
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireActivity()).setView(view)
        val targetSelectionDialog = adb.create()
        if (targetSelectionDialog != null && targetSelectionDialog.window != null) {
            targetSelectionDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val imageFile = File(requireActivity().externalCacheDir, "${transaction.transactionId}.jpg")
        if (imageFile.exists()) {
            imageHolder.setImageURI(FileProvider.getUriForFile(requireActivity(), "${requireActivity().applicationContext.packageName}.provider", imageFile))
        }

        makePhoto.setOnClickListener {
            requestCameraPermission()
        }

        choosePhoto.setOnClickListener {
            requestGalleryPermission()
        }

        return targetSelectionDialog
    }

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ContextCompat.checkSelfPermission(requireActivity(), READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery.launch("image/*")
            } else {
                requestPermissionsCustom.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED))
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireActivity(), READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery.launch("image/*")
            } else {
                requestPermissionsCustom.launch(arrayOf(READ_MEDIA_IMAGES))
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery.launch("image/*")
            } else {
                requestPermissionsCustom.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            imageUri = createImageUri()
            takePicture.launch(imageUri)
        } else {
            requestPermissionsCustom.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun createImageUri(): Uri {
        val imageFile = File(requireActivity().externalCacheDir, "${transaction.transactionId}.jpg")
        return FileProvider.getUriForFile(requireActivity(), "${requireActivity().applicationContext.packageName}.provider", imageFile)
    }

    private fun copyImageFromUri(uri: Uri) {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val outputStream = requireActivity().contentResolver.openOutputStream(createImageUri())
        try {
            if (inputStream != null && outputStream != null) {
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }


    private fun bindViews(view: View) {
        makePhoto = view.findViewById(R.id.imsedial_fromCamera_btn)
        choosePhoto = view.findViewById(R.id.imsedial_fromGallery_btn)
        imageHolder = view.findViewById(R.id.imsedial_imageHolder_img)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            galleryRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery.launch("image/*")
                } else {
                    Toast.makeText(requireActivity(), "Gallery access denied", Toast.LENGTH_SHORT).show()
                }
            }
            cameraRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageUri = createImageUri()
                    takePicture.launch(imageUri)
                } else {
                    Toast.makeText(requireActivity(), "Camera access denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}