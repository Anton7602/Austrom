package com.colleagues.austrom.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.AssetCreationDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView

class BalanceFragment : Fragment(R.layout.fragment_balance) {
    private lateinit var addNewAssetButton: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        addNewAssetButton.setOnClickListener {
            AssetCreationDialogFragment().show(requireActivity().supportFragmentManager, "Asset Creation Dialog")
        }
    }

    private fun bindViews(view: View) {
        addNewAssetButton = view.findViewById(R.id.bal_addNew_fab)
    }
}