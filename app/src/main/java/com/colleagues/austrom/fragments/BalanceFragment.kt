package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.AssetCreationDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BalanceFragment : Fragment(R.layout.fragment_balance) {
    private lateinit var assetHolderRecyclerView: RecyclerView
    private lateinit var addNewAssetButton: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        if (AustromApplication.activeAssets.isEmpty()) {
            updateAssetsList()
        }
        setUpRecyclerView()

        addNewAssetButton.setOnClickListener {
            AssetCreationDialogFragment(this).show(requireActivity().supportFragmentManager, "Asset Creation Dialog")
        }
    }

    fun updateAssetsList() {
        val dbProvider : IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
        val user = AustromApplication.appUser
        if (user!=null) {
            val activeAssets = if (user.activeBudgetId!=null) {
                val budget = dbProvider.getBudgetById(user.activeBudgetId!!)
                if (budget!=null) {
                    dbProvider.getAssetsOfBudget(budget)
                } else {
                    dbProvider.getAssetsOfUser(user)
                }
            } else {
                dbProvider.getAssetsOfUser(user)
            }
            if (activeAssets.isNotEmpty()) {
                AustromApplication.activeAssets = activeAssets
            }
        }
    }

    private fun setUpRecyclerView() {
        assetHolderRecyclerView.layoutManager = LinearLayoutManager(activity)
        assetHolderRecyclerView.adapter = AssetRecyclerAdapter(AustromApplication.activeAssets)
    }

    private fun bindViews(view: View) {
        assetHolderRecyclerView = view.findViewById(R.id.bal_assetHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.bal_addNew_fab)
    }
}