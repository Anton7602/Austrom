package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.Manifest
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class ImageSelectionDialogFragment(private val transaction: Transaction) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_image_selection, container, false) }
    fun setOnDialogResultListener(l: (Boolean)->Unit) { returnResult = l }
    private var returnResult: (Boolean)->Unit = {}
    //region Binding
    private lateinit var closeButton: ImageButton
    private lateinit var makePhotoButton: Button
    private lateinit var choosePhotoButton: Button
    private lateinit var imageHolder: ImageView
    private lateinit var messageText: TextView
    private lateinit var imageUri: Uri
    private lateinit var dialogHolder: CardView
    private fun bindViews(view: View) {
        makePhotoButton = view.findViewById(R.id.imsedial_fromCamera_btn)
        choosePhotoButton = view.findViewById(R.id.imsedial_fromGallery_btn)
        imageHolder = view.findViewById(R.id.imsedial_imageHolder_img)
        messageText = view.findViewById(R.id.imsedial_noImageMessage_txt)
        closeButton = view.findViewById(R.id.imsedial_close_btn)
        dialogHolder = view.findViewById(R.id.imsedial_holder_crv)
    }
    // endregion

    private val cameraRequestCode = 100
    private val galleryRequestCode = 101
    private val requestPermissionsCustom = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.keys.elementAt(0) == "android.permission.CAMERA") {
                requestCameraPermission()
            } else {
                requestGalleryPermission()
            }
    }
    private val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            copyImageFromUri(uri)
            imageHolder.setImageURI(null)
            imageHolder.setImageURI(uri)
            messageText.visibility = View.GONE
            imageHolder.visibility = View.VISIBLE
            returnResult(true)
        }
    }
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            imageHolder.setImageURI(null)
            imageHolder.setImageURI(imageUri)
            messageText.visibility = View.GONE
            imageHolder.visibility = View.VISIBLE
            returnResult(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)

        val imageFile = File(requireActivity().externalCacheDir, "${transaction.transactionId}.jpg")
        if (imageFile.exists()) {
            imageHolder.setImageURI(FileProvider.getUriForFile(requireActivity(), "${requireActivity().applicationContext.packageName}.provider", imageFile))
            messageText.visibility = View.GONE
            imageHolder.visibility = View.VISIBLE
        } else {
            messageText.visibility = View.VISIBLE
            imageHolder.visibility = View.GONE
        }

        imageHolder.setOnClickListener {
            closeButton.visibility= if (closeButton.visibility==View.VISIBLE) View.GONE else View.VISIBLE
            makePhotoButton.visibility= if (makePhotoButton.visibility==View.VISIBLE) View.GONE else View.VISIBLE
            choosePhotoButton.visibility= if (choosePhotoButton.visibility==View.VISIBLE) View.GONE else View.VISIBLE
        }

        makePhotoButton.setOnClickListener {
            requestCameraPermission()
        }

        choosePhotoButton.setOnClickListener {
            requestGalleryPermission()
        }

        closeButton.setOnClickListener {
            dismiss()
        }
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