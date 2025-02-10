package com.colleagues.austrom.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class AssetEditFragment(private val asset: Asset) : Fragment(R.layout.fragment_asset_edit) {
    fun setOnDialogResultListener(l: ((Asset)->Unit)) { returnResult = l }
    private var returnResult: (Asset)->Unit = { _->}
    //region Binding
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var assetAmountTextView: TextInputEditText
    private lateinit var assetAmountLayout: TextInputLayout
    private lateinit var assetNameTextView: TextInputEditText
    private lateinit var assetNameLayout: TextInputLayout
    private lateinit var currencyNameTextView: TextView
    private fun bindViews(view: View) {
        cancelButton = view.findViewById(R.id.assed_cancel_btn)
        saveButton = view.findViewById(R.id.assed_accept_btn)
        assetAmountTextView = view.findViewById(R.id.assed_assetAmount_txt)
        assetNameTextView = view.findViewById(R.id.assed_assetName_txt)
        assetAmountLayout = view.findViewById(R.id.assed_assetAmount_til)
        assetNameLayout = view.findViewById(R.id.assed_assetName_til)
        currencyNameTextView = view.findViewById(R.id.assed_currencySymbol_txt)
    }
    //endregion
    private var assetNameTextListener: TextWatcher? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpAsset()
        cancelButton.setOnClickListener { returnResult(asset) }
        saveButton.setOnClickListener {save()}

    }

    private fun setUpAsset() {
        assetAmountTextView.setText("%.2f".format(asset.amount))
        assetNameTextView.setText(asset.assetName)
        currencyNameTextView.text = asset.currencyCode
    }

    private fun save() {
        if (!isInputValid()) return
        asset.assetName = assetNameTextView.text.toString()
        asset.amount = assetAmountTextView.text.toString().parseToDouble() ?: 0.0
        returnResult(asset)
    }

    private fun isInputValid(): Boolean {
        if (!isAssetNameValid()) return false
        if (!isAssetAmountValid()) return false
        return true
    }

    private fun isAssetNameValid(): Boolean {
        if (assetNameTextListener==null)
            assetNameTextListener = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {isAssetNameValid()}
                override fun afterTextChanged(s: Editable?) {}
            }
        val assetName = assetNameTextView.text.toString()
        if (assetName.isEmpty()) {
            assetNameLayout.error = getString(R.string.asset_s_title_cannot_be_empty)
            assetNameTextView.addTextChangedListener(assetNameTextListener)
            return false
        } else {
            assetNameLayout.error = null
            assetNameTextView.removeTextChangedListener(assetNameTextListener)
            return true
        }
    }

    private fun isAssetAmountValid(): Boolean {
        val amount = assetAmountTextView.text.toString().parseToDouble()
        if (amount == null) {
            assetAmountLayout.error = "Amount Value is invalid"
            return false
        }
        else {
            assetAmountLayout.error = null
            return true
        }
    }
}