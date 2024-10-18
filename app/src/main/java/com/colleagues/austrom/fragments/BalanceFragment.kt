package com.colleagues.austrom.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetGroupRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.AssetCreationDialogFragment
import com.colleagues.austrom.dialogs.AssetFilter
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Asset
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BalanceFragment : Fragment(R.layout.fragment_balance) {
    private lateinit var assetHolderRecyclerView: RecyclerView
    private lateinit var addNewAssetButton: FloatingActionButton
    private lateinit var totalAmountText: TextView
    private lateinit var baseCurrencySymbolText: TextView
    var activeFilter: AssetFilter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        if (AustromApplication.activeAssets.isEmpty()) { updateAssetsList() }

        addNewAssetButton.setOnClickListener {
            AssetCreationDialogFragment(this).show(requireActivity().supportFragmentManager, "Asset Creation Dialog")
        }
    }

    override fun onResume() {
        super.onResume()
        updateAssetsList()
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
                val filteredAssets = (activeAssets.filter { entry ->
                    !entry.value.isPrivate || entry.value.userId==AustromApplication.appUser?.userId }).toMutableMap()
                AustromApplication.activeAssets = filteredAssets
            }
        }
        setUpRecyclerView(AustromApplication.activeAssets)
        calculateTotalAmount(AustromApplication.activeAssets)
    }

    fun filterAssets(filter: AssetFilter) {
        activeFilter = filter
        var filteredAssets = AustromApplication.activeAssets.toMap()
        if (!filter.showShared) {
            filteredAssets = filteredAssets.filter { entry -> entry.value.userId==AustromApplication.appUser?.userId }
        }
        setUpRecyclerView(filteredAssets.toMutableMap())
        calculateTotalAmount(filteredAssets.toMutableMap())
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotalAmount(assetList: MutableMap<String, Asset>) {
        baseCurrencySymbolText.text = AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol
        var totalAmount = 0.0
        for (asset in assetList) {
            totalAmount += if (asset.value.currencyCode==AustromApplication.appUser?.baseCurrencyCode) {
                asset.value.amount
            } else {
                asset.value.amount/(AustromApplication.activeCurrencies[asset.value.currencyCode]?.exchangeRate ?: 1.0)
            }
        }
        //Temporary fix - both sum and currency are baked into one string to properly autoscale font. Split it in free time
        totalAmountText.text = "${totalAmount.toMoneyFormat()} ${AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol}"
    }

    private fun setUpRecyclerView(assetList: MutableMap<String, Asset>) {
        assetHolderRecyclerView.layoutManager = LinearLayoutManager(activity)
        val groupedAssets = Asset.groupAssetsByType(assetList)
        assetHolderRecyclerView.adapter = AssetGroupRecyclerAdapter(groupedAssets, (requireActivity() as AppCompatActivity))
    }

    private fun bindViews(view: View) {
        assetHolderRecyclerView = view.findViewById(R.id.bal_assetHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.bal_addNew_fab)
        totalAmountText = view.findViewById(R.id.bal_totalAmount_txt)
        baseCurrencySymbolText = view.findViewById(R.id.bal_baseCurrencySymbol_txt)
    }
}