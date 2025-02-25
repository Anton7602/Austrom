package com.colleagues.austrom.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AssetCreationActivity
import com.colleagues.austrom.AssetPropertiesActivity
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetGroupRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.AssetFilter
import com.colleagues.austrom.dialogs.bottomsheetdialogs.AssetTypeSelectionDialogFragment
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.views.DateControllerView
import com.colleagues.austrom.views.MoneyFormatTextView

class BalanceFragment : Fragment(R.layout.fragment_balance) {
    //region Binding
    private lateinit var assetHolderRecyclerView: RecyclerView
    private lateinit var addNewAssetButton: ImageButton
    private lateinit var totalAmountText: MoneyFormatTextView
    private lateinit var callNavigationDrawerButton: ImageButton
    private fun bindViews(view: View) {
        assetHolderRecyclerView = view.findViewById(R.id.bal_assetHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.bal_createNewAsset_btn)
        totalAmountText = view.findViewById(R.id.bal_totalAmout_mtxt)
        callNavigationDrawerButton = view.findViewById(R.id.bal_navDrawer_btn)
    }
    //endregion
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    var activeFilter: AssetFilter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        totalAmountText.setValue(0.0, AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]!!)
        if (AustromApplication.activeAssets.isEmpty()) { updateAssetsList() }
        addNewAssetButton.setOnSafeClickListener { launchNewAssetCreationDialog()  }
        callNavigationDrawerButton.setOnClickListener { requestNavigationDrawerOpen() }
    }

    private fun launchNewAssetCreationDialog() {
        val dialog = AssetTypeSelectionDialogFragment()
        dialog.setOnDialogResultListener { assetType ->
            if (assetType.isLiability)
                startAssetCreationActivity(assetType, arrayListOf(AssetType.CREDIT_CARD.ordinal, AssetType.LOAN.ordinal, AssetType.MORTAGE.ordinal))
            else
                startAssetCreationActivity(assetType, arrayListOf(AssetType.CARD.ordinal, AssetType.CASH.ordinal, AssetType.DEPOSIT.ordinal, AssetType.INVESTMENT.ordinal,))
        }
        dialog.show(requireActivity().supportFragmentManager, "AssetTypeSelectionDialog")
    }

    private fun updateAssetsList() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        localDBProvider.getAssetsByAssetFilterAsync().observe(viewLifecycleOwner) {assetList ->
            AustromApplication.activeAssets = mutableMapOf()
            assetList.forEach { asset -> AustromApplication.activeAssets[asset.assetId] = asset }
            setUpRecyclerView(AustromApplication.activeAssets)
            calculateTotalAmount(AustromApplication.activeAssets)
        }
//        val user = AustromApplication.appUser
//        if (user!=null) {
//            val activeAssets =  dbProvider.getAssetsOfBudget()
//            if (activeAssets.isNotEmpty()) {
//                val filteredAssets = (activeAssets.filter { entry ->
//                    !entry.value.isPrivate || entry.value.userId==AustromApplication.appUser?.userId }).toMutableMap()
//                AustromApplication.activeAssets = filteredAssets
//            } else {
//                AustromApplication.activeAssets = mutableMapOf()
//            }
//        }
//        setUpRecyclerView(AustromApplication.activeAssets)
//        calculateTotalAmount(AustromApplication.activeAssets)
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

    private fun startAssetCreationActivity(selectedAssetType: AssetType , assetTypes: ArrayList<Int>) {
        val intent = Intent(requireActivity(), AssetCreationActivity::class.java)
        intent.putExtra("AssetType", selectedAssetType)
        intent.putExtra("ListOfAvailableAssetTypes", assetTypes)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        updateAssetsList()
    }
}