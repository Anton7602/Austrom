package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetGroupRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.AssetCreationDialogFragment
import com.colleagues.austrom.models.Asset
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BalanceFragment : Fragment(R.layout.fragment_balance) {
    private lateinit var assetHolderRecyclerView: RecyclerView
    private lateinit var addNewAssetButton: FloatingActionButton
    private lateinit var totalAmountText: TextView
    private lateinit var baseCurrencySymbolText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        if (AustromApplication.activeAssets.isEmpty()) {
            updateAssetsList()
        } else {
            setUpRecyclerView()
        }
        calculateTotalAmount()
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
        setUpRecyclerView()
    }

    private fun calculateTotalAmount() {
        baseCurrencySymbolText.text = AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol
        var totalAmount = 0.0
        for (asset in AustromApplication.activeAssets) {
            totalAmount += if (asset.value.currencyCode==AustromApplication.appUser?.baseCurrencyCode) {
                asset.value.amount
            } else {
                asset.value.amount/(AustromApplication.activeCurrencies[asset.value.currencyCode]?.exchangeRate ?: 1.0)
            }
        }
        totalAmountText.text = String.format("%.2f", totalAmount)
    }

    private fun setUpRecyclerView() {
        assetHolderRecyclerView.layoutManager = LinearLayoutManager(activity)
//        assetHolderRecyclerView.adapter = AssetRecyclerAdapter(AustromApplication.activeAssets)
        val groupedAssets = Asset.groupAssetsByType(AustromApplication.activeAssets)
        assetHolderRecyclerView.adapter = AssetGroupRecyclerAdapter(groupedAssets, requireActivity())
    }

    private fun bindViews(view: View) {
        assetHolderRecyclerView = view.findViewById(R.id.bal_assetHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.bal_addNew_fab)
        totalAmountText = view.findViewById(R.id.bal_totalAmount_txt)
        baseCurrencySymbolText = view.findViewById(R.id.bal_baseCurrencySymbol_txt)
    }
}