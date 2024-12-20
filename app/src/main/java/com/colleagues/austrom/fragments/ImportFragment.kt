package com.colleagues.austrom.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.colleagues.austrom.ImportParametersActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.views.SettingsButtonView
import com.opencsv.CSVReader
import java.io.InputStreamReader


class ImportFragment : Fragment(R.layout.fragment_import) {
    //region Binding
    private lateinit var importFromCSVFileButton: SettingsButtonView
    private fun bindViews(view: View) {
        importFromCSVFileButton = view.findViewById(R.id.imprt_fromCsv_btn)
    }
    //endregion
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        filePickerLauncher = initializeActivityForResult()
        importFromCSVFileButton.setOnClickListener {  pickCsvFile() }
    }

    private fun initializeActivityForResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    startActivity(Intent(requireActivity(), ImportParametersActivity::class.java).putExtra("FilePath", uri))
                    //readCsvFile(uri)
                }
            }
        }
    }

    private fun pickCsvFile() {
        filePickerLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        })
    }
}