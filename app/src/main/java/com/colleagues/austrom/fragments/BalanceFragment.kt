package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetRecyclerAdapter
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.dialogs.AssetCreationDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BalanceFragment : Fragment(R.layout.fragment_balance) {
    private lateinit var assetHolderRecyclerView: RecyclerView
    private lateinit var addNewAssetButton: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        assetHolderRecyclerView.layoutManager = LinearLayoutManager(activity)
        assetHolderRecyclerView.adapter = AssetRecyclerAdapter((requireActivity().application as AustromApplication).activeAssets)

        addNewAssetButton.setOnClickListener {
            AssetCreationDialogFragment().show(requireActivity().supportFragmentManager, "Asset Creation Dialog")
        }
    }

    private fun bindViews(view: View) {
        assetHolderRecyclerView = view.findViewById(R.id.bal_assetHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.bal_addNew_fab)
    }
}