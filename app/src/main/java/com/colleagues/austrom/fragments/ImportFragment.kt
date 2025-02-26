package com.colleagues.austrom.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.colleagues.austrom.ExportActivity
import com.colleagues.austrom.ImportParametersActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.views.SettingsButtonView

class ImportFragment : Fragment(R.layout.fragment_import) {
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var importFromCSVFileButton: SettingsButtonView
    private lateinit var exportToCSVFileButton: SettingsButtonView
    private lateinit var callNavDrawerButton: ImageButton
    private fun bindViews(view: View) {
        importFromCSVFileButton = view.findViewById(R.id.imprt_fromCsv_btn)
        exportToCSVFileButton = view.findViewById(R.id.imprt_toCsv_btn)
        callNavDrawerButton = view.findViewById(R.id.imprt_navDrawer_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        importFromCSVFileButton.setOnClickListener { startActivity(Intent(requireActivity(), ImportParametersActivity::class.java)) }
        exportToCSVFileButton.setOnClickListener { startActivity(Intent(requireActivity(), ExportActivity::class.java)) }
        callNavDrawerButton.setOnClickListener { requestNavigationDrawerOpen() }
    }
}