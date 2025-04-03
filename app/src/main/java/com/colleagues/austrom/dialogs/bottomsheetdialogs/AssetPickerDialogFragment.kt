package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.cardview.widget.CardView.TEXT_ALIGNMENT_CENTER
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetTypeRecyclerAdapter
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.models.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel

class AssetPickerDialogFragment(private val assetList: Map<Asset, Boolean>) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_asset_picker, container, false) }
    fun setOnSelectionChangedListener(l: ((selectionState: Boolean, asset: Asset)->Unit)) { notifySelectionChanged = l }
    private var notifySelectionChanged: (Boolean, Asset) -> Unit = { _, _ -> }
    //region Binding
    private lateinit var assetHolder: ChipGroup
    private lateinit var dialogHolder: CardView
    private lateinit var inverseButton: ImageButton
    private fun bindViews(view: View) {
        assetHolder = view.findViewById(R.id.aspickdial_assetHolder_chg)
        dialogHolder = view.findViewById(R.id.aspickdial_holder_crv)
        inverseButton = view.findViewById(R.id.aspickdial_inverseSelection_btn)
    }
    ///endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        inverseButton.setOnSafeClickListener { assetHolder.children.forEach { child -> if (child is Chip) { child.isChecked = !child.isChecked; notifySelectionChanged(child.isChecked, child.tag as Asset) } } }
        setUpAssetsInChips(assetList)
    }

    private fun setUpAssetsInChips(assets: Map<Asset, Boolean>) {
        assets.forEach { entry ->
            val asset = entry.key
            assetHolder.addView(Chip(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(context.dpToPx(8)).build()
                chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_assettype_card_temp)
                chipBackgroundColor = (context.getColorStateList(R.color.chip_filter_background_color))
                setChipIconTintResource(R.color.chip_filter_text_color)
                setTextColor(context.getColorStateList(R.color.chip_filter_text_color))
                setPadding(0,context.dpToPx(0).toInt(),0,context.dpToPx(0).toInt())
                textAlignment = TEXT_ALIGNMENT_CENTER
                setEnsureMinTouchTargetSize(false)
                minimumHeight = 0
                text = asset.assetName
                isChipIconVisible = true
                isCheckable = true
                isChecked = entry.value
                tag = asset
                setOnClickListener { _ -> notifySelectionChanged(this.isChecked, asset)
//                    if (this.isChecked) {
//                        transactionFilter.assets.add(chip.tag.toString())
//                        chipHeader.isChecked = true
//                    } else {
//                        transactionFilter.assets.remove(chip.tag.toString())
//                        chipHeader.isChecked = false
//                        chipGroup.children.forEach { view -> if (view is Chip && view.isChecked) chipHeader.isChecked = true; }
//                    }
//                    returnFilter(transactionFilter)
                }
            })
            //transactionFilter.assets.add(asset.assetId)
        }
    }
}