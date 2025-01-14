package com.colleagues.austrom.dialogs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AssetCreationActivity
import com.colleagues.austrom.LiabilityCreationActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetTypeRecyclerAdapter
import com.colleagues.austrom.models.AssetType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AssetTypeSelectionDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_asset_type_selection, container, false) }
    fun setOnDialogResultListener(l: (AssetType)->Unit) { returnResult = l }
    private var returnResult: (AssetType)->Unit = {}
    //region Binding
    private lateinit var assetTypeHolder: RecyclerView
    private lateinit var liabilityTypeHolder: RecyclerView
    private lateinit var dialogHolder: CardView
    private lateinit var assetSwitchButton: Button
    private lateinit var liabilitySwitchButton: Button
    private fun bindViews(view: View) {
        assetTypeHolder = view.findViewById(R.id.astypeseldial_assetHolder_rcv)
        liabilityTypeHolder = view.findViewById(R.id.astypeseldial_liabilityHolder_rcv)
        dialogHolder = view.findViewById(R.id.astypeseldial_holder_crv)
        assetSwitchButton = view.findViewById(R.id.astypeseldial_asset_btn)
        liabilitySwitchButton = view.findViewById(R.id.astypeseldial_liability_btn)
    }
    ///endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        setUpRecyclerViews()

        assetSwitchButton.setOnClickListener { switchAssetTypeVisibility(false) }
        liabilitySwitchButton.setOnClickListener { switchAssetTypeVisibility(true) }
    }

    private fun switchAssetTypeVisibility(isLiabilitiesSelected: Boolean) {
        assetTypeHolder.visibility = if (isLiabilitiesSelected) View.GONE else View.VISIBLE
        liabilityTypeHolder.visibility = if (isLiabilitiesSelected) View.VISIBLE else View.GONE
        assetSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(if (isLiabilitiesSelected) R.color.grey else R.color.blue))
        liabilitySwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(if (isLiabilitiesSelected) R.color.blue else R.color.grey))
    }


    private fun setUpRecyclerViews() {
        assetTypeHolder.layoutManager = LinearLayoutManager(activity)
        val adapterAssetType = AssetTypeRecyclerAdapter(AssetType.entries.filter { l -> !l.isLiability }, requireActivity() as AppCompatActivity)
        adapterAssetType.setOnItemClickListener { assetType -> returnResult(assetType); dismiss() }
        assetTypeHolder.adapter = adapterAssetType

        liabilityTypeHolder.layoutManager = LinearLayoutManager(activity)
        val adapterLiabilityType = AssetTypeRecyclerAdapter(AssetType.entries.filter { l -> l.isLiability }, requireActivity() as AppCompatActivity)
        adapterLiabilityType.setOnItemClickListener { assetType -> returnResult(assetType); dismiss() }
        liabilityTypeHolder.adapter = adapterLiabilityType
    }
}