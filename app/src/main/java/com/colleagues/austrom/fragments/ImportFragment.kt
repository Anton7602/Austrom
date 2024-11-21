package com.colleagues.austrom.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.colleagues.austrom.ImportParametersActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.views.SettingsButtonView
import com.opencsv.CSVReader
import java.io.InputStreamReader


class ImportFragment : Fragment(R.layout.fragment_import) {
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var importFromCSVFileButton: SettingsButtonView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    startActivity(Intent(requireActivity(), ImportParametersActivity::class.java).putExtra("FilePath", uri))
                    //readCsvFile(uri)
                }
            }
        }

        importFromCSVFileButton.setOnClickListener {
            pickCsvFile()
        }
    }

    private fun pickCsvFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/csv"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun bindViews(view: View) {
        importFromCSVFileButton = view.findViewById(R.id.imprt_fromCsv_btn)
    }
}