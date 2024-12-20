package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.models.AssetValidationType
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.InvalidAssetException
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class AssetCreationDialogFragment(private val parentDialog: BalanceFragment?) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {return inflater.inflate(R.layout.dialog_fragment_asset_creation, container, false) }
    fun setOnDialogResultListener(l: (()->Unit)) { returnResult = l }
    private var returnResult: ()->Unit = {}
    //region Binding
    private lateinit var titleTextView: TextInputEditText
    private lateinit var amountTextView: TextInputEditText
    private lateinit var typeChipGroup: ChipGroup
    private lateinit var currencyChipGroup: ChipGroup
    private lateinit var createNewAssetButton: Button
    private lateinit var dialogHolder: CardView
    private fun bindViews(view: View) {
        titleTextView = view.findViewById(R.id.cadial_title_txt)
        amountTextView = view.findViewById(R.id.cadial_amount_txt)
        typeChipGroup = view.findViewById(R.id.cadial_assetType_chpgrp)
        currencyChipGroup = view.findViewById(R.id.cadial_currency_chpgrp)
        createNewAssetButton = view.findViewById(R.id.cadial_createAsset_btn)
        dialogHolder = view.findViewById(R.id.cadial_holder_crv)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        createNewAssetButton.setOnClickListener { createNewAsset(view) }
    }

    private fun createNewAsset(view: View) {
        val dbProvider = LocalDatabaseProvider(requireActivity())
        val assetType : Chip = view.findViewById(typeChipGroup.checkedChipId)
        val currencyType : Chip = view.findViewById(currencyChipGroup.checkedChipId)
        if (titleTextView.text.toString().isNotEmpty()) {
            try {
                Asset(
                    assetTypeId = getTypeID(assetType.tag.toString()) ?: throw InvalidAssetException(AssetValidationType.UNKNOWN_ASSET_TYPE_INVALID),
                    assetName = titleTextView.text.toString(),
                    amount = if (amountTextView.text.toString().isNotEmpty()) amountTextView.text.toString().toDouble() else 0.0,
                    currencyCode = currencyType.text.toString(),
                ).add(dbProvider)
                parentDialog?.updateAssetsList()
                this.dismiss()
            } catch (ex: InvalidAssetException) {
                Toast.makeText(requireActivity(), ex.message, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireActivity(),
                getString(R.string.asset_s_title_cannot_be_empty), Toast.LENGTH_LONG).show()
        }
    }

    //REDO!!!!
    private fun getTypeID(typeName : String) : AssetType? {
        when(typeName) {
            "CARD" -> return AssetType.CARD
            "CASH" -> return AssetType.CASH
            "INVESTMENT" -> return AssetType.INVESTMENT
        }
        return null
    }
}