package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class TransactionCreationDialogFragment : BottomSheetDialogFragment() {
    private lateinit var sumText: EditText
    private lateinit var fromCard: CardView
    private lateinit var fromName: TextView
    private lateinit var toCard: CardView
    private lateinit var toName: TextView
    private lateinit var categoryChips: ChipGroup
    private lateinit var dateChips: ChipGroup
    private lateinit var submitButton : Button
    private var selectedAsset : Asset? = null
    private var selectedTarget : String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpCategoriesInChips()
        setUpDateChips()
        sumText.requestFocus()

        fromCard.setOnClickListener {
            AssetSelectionDialogFragment((requireActivity().application as AustromApplication).activeAssets, this)
                .show(requireActivity().supportFragmentManager, "Asset Selection Dialog")
        }

        toCard.setOnClickListener {
            TargetSelectionDialogFragment(this).show(requireActivity().supportFragmentManager, "Target Selection Dialog")
        }

        submitButton.setOnClickListener {
            val provider = FirebaseDatabaseProvider()
            val categoryChip : Chip = view.findViewById(categoryChips.checkedChipId)
            val dateChip : Chip = view.findViewById(dateChips.checkedChipId)
            val dateInt = provider.parseDateToIntDate(dateChip.tag as LocalDate)
            provider.writeNewTransaction(Transaction(
                userID = (requireActivity().application as AustromApplication).appUser?.userID,
                sourceID = selectedAsset?.assetID,
                sourceName = selectedAsset?.assetName,
                targetID = null,
                targetName = selectedTarget,
                amount = sumText.text.toString().toDouble(),
                currency = selectedAsset?.currency_id.toString(),
                categoryID = categoryChip.text.toString(),
                transactionDate = null,
                transactionDateInt = dateInt,
                comment = null
            ))
            dismiss()
            //Toast.makeText(requireActivity(), "Category: ${category.text}; Date: ${date.tag}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_transaction_creation, container, false)
    }

    fun receiveTargetSelection(target: String) {
        selectedTarget = target
        toName.text = target
    }

    fun receiveAssetSelection(asset: Asset) {
        selectedAsset = asset
        fromName.text = asset.assetName
    }

    private fun setUpCategoriesInChips() {
        val categories = (requireActivity().application as AustromApplication).defaultCategories
        for (category in categories) {
            val chip = Chip(requireActivity())
            chip.text = category.name
            chip.chipIcon = ContextCompat.getDrawable(requireActivity(), category.imgReference ?: R.drawable.ic_placeholder_icon)
            chip.setEnsureMinTouchTargetSize(false)
            chip.isCheckable = true
//            chip.setChipDrawable(
//                ChipDrawable.createFromAttributes(
//                    requireActivity(),
//                    null,
//                    0,
//                    R.style.Widget_MaterialComponents_Chip_Choice
//                )
//            )
            if (category == categories[0]) {
                chip.isChecked = true
            }
            categoryChips.addView(chip)
        }
    }

    private fun setUpDateChips() {
        var chipDate = LocalDate.now()
        for (i in 0..9) {
            val chip = Chip(requireActivity())
            var chipDayOfWeek = chipDate.dayOfWeek.toString()
            chipDayOfWeek = chipDayOfWeek[0].titlecase() + chipDayOfWeek.lowercase().substring(1)
            chip.text = "${chipDayOfWeek} ${chipDate.format(DateTimeFormatter.ofPattern("dd.MM"))}"
            chip.setEnsureMinTouchTargetSize(false)
            chip.isCheckable = true
            chip.tag = chipDate
            if (i == 0) {
                chip.isChecked = true
            }
            chipDate = chipDate.minusDays(1)
            dateChips.addView(chip)
        }
    }

    private fun bindViews(view: View) {
        sumText = view.findViewById(R.id.ctdial_sum_txt)
        fromCard = view.findViewById(R.id.ctdial_cardFrom_crv)
        fromName = view.findViewById(R.id.ctdial_fromName_txt)
        toCard = view.findViewById(R.id.ctdial_cardTo_crv)
        toName = view.findViewById(R.id.ctdial_toName_txt)
        categoryChips = view.findViewById(R.id.ctdial_categoriesChips_cgr)
        dateChips = view.findViewById(R.id.ctdial_datesChips_cgr)
        submitButton = view.findViewById(R.id.ctdial_submit_btn)
    }
}