package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class AssetCreationDialogFragment(private val parentDialog: BalanceFragment?) : BottomSheetDialogFragment() {
    private lateinit var titleTextView: TextInputEditText
    private lateinit var amountTextView: TextInputEditText
    private lateinit var typeChipGroup: ChipGroup
    private lateinit var currencyChipGroup: ChipGroup
    private lateinit var createNewAssetButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_asset_creation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        createNewAssetButton.setOnClickListener {
            val provider : IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
            val assetType : Chip = view.findViewById(typeChipGroup.checkedChipId)
            val currencyType : Chip = view.findViewById(currencyChipGroup.checkedChipId)
            provider.createNewAsset(
                Asset(
                    assetTypeId = getTypeID(assetType.text.toString()),
                    assetName = titleTextView.text.toString(),
                    userId = (requireActivity().application as AustromApplication).appUser?.userId.toString(),
                    amount = amountTextView.text.toString().toDouble(),
                    currencyId = currencyType.text.toString(),
                    isPrivate = false
                )
            )
            parentDialog?.updateAssetsList()
            this.dismiss()
        }
    }

    //REDO!!!!
    private fun getTypeID(typeName : String) : AssetType? {
        when(typeName) {
            "Card" -> return AssetType.CARD
            "Cash" -> return AssetType.CASH
            "Investment" -> return AssetType.INVESTMENT
        }
        return null
    }

    private fun bindViews(view: View) {
        titleTextView = view.findViewById(R.id.cadial_title_txt)
        amountTextView = view.findViewById(R.id.cadial_amount_txt)
        typeChipGroup = view.findViewById(R.id.cadial_assetType_chpgrp)
        currencyChipGroup = view.findViewById(R.id.cadial_currency_chpgrp)
        createNewAssetButton = view.findViewById(R.id.cadial_createAsset_btn)
    }
}