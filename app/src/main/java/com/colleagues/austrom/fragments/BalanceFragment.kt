package com.colleagues.austrom.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AssetCreationActivity
import com.colleagues.austrom.AssetPropertiesActivity
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.LiabilityCreationActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetGroupRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.AssetFilter
import com.colleagues.austrom.dialogs.AssetTypeSelectionDialogFragment
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.views.MoneyFormatTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BalanceFragment : Fragment(R.layout.fragment_balance) {
    //region Binding
    private lateinit var assetHolderRecyclerView: RecyclerView
    private lateinit var addNewAssetButton: ImageButton
    private lateinit var totalAmountText: MoneyFormatTextView
    private fun bindViews(view: View) {
        assetHolderRecyclerView = view.findViewById(R.id.bal_assetHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.bal_createNewAsset_btn)
        totalAmountText = view.findViewById(R.id.bal_totalAmout_mtxt)
    }
    //endregion
    var activeFilter: AssetFilter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        if (AustromApplication.activeAssets.isEmpty()) { updateAssetsList() }
        addNewAssetButton.setOnClickListener { launchNewAssetCreationDialog()  }
    }

    private fun launchNewAssetCreationDialog() {
        val dialog = AssetTypeSelectionDialogFragment()
        dialog.setOnDialogResultListener { assetType ->
            if (assetType.isLiability)
                startActivity(Intent(requireActivity(), LiabilityCreationActivity::class.java).putExtra("AssetType", assetType))
            else
                startActivity(Intent(requireActivity(), AssetCreationActivity::class.java).putExtra("AssetType", assetType))
        }
        dialog.show(requireActivity().supportFragmentManager, "AssetTypeSelectionDialog")
    }

    fun updateAssetsList() {
        val dbProvider = LocalDatabaseProvider(requireActivity())
        val user = AustromApplication.appUser
        if (user!=null) {
            val activeAssets = if (user.activeBudgetId!=null) {
                val budget = Budget()
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
            } else {
                AustromApplication.activeAssets = mutableMapOf()
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
        var totalAmount = 0.0
        for (asset in assetList) {
            totalAmount += if (asset.value.currencyCode==AustromApplication.appUser?.baseCurrencyCode) {
                asset.value.amount
            } else {
                asset.value.amount/(AustromApplication.activeCurrencies[asset.value.currencyCode]?.exchangeRate ?: 1.0)
            }
        }
        totalAmountText.setValue(totalAmount)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpRecyclerView(assetList: MutableMap<String, Asset>) {
        assetHolderRecyclerView.layoutManager = LinearLayoutManager(activity)
        val groupedAssets = Asset.groupAssetsByType(assetList)
        val adapter = AssetGroupRecyclerAdapter(groupedAssets, (requireActivity() as AppCompatActivity))
        adapter.setOnItemClickListener { asset -> requireActivity().startActivity(Intent(activity, AssetPropertiesActivity::class.java).putExtra("assetId", asset.assetId)) }
        assetHolderRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        updateAssetsList()
    }
}