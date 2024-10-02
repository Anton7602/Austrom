package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionCreationDialogFragment(private val parentDialog: OpsFragment,
                                        private val transactionType: TransactionType) : BottomSheetDialogFragment() {
    private lateinit var sumText: EditText
    private lateinit var sumReceivedText: EditText
    private lateinit var fromCard: CardView
    private lateinit var fromName: TextView
    private lateinit var toCard: CardView
    private lateinit var toName: TextView
    private lateinit var categoryChips: ChipGroup
    private lateinit var dateChips: ChipGroup
    private lateinit var submitButton : Button
    private lateinit var calendarButton: ImageButton
    private lateinit var currencySymbol: TextView
    private lateinit var currencySymbolReceived: TextView
    private var selectedSource : Asset? = null
    private var sourceName: String? = null
    private var selectedTarget : Asset? = null
    private var targetName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpCategoriesInChips()
        setUpDateChips()
        setUpPrimaryAsset()
        currencySymbol.text = AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol
        sumText.requestFocus()

        fromCard.setOnClickListener {
            if (transactionType==TransactionType.EXPENSE || transactionType==TransactionType.TRANSFER) {
                AssetSelectionDialogFragment(true, AustromApplication.activeAssets, this)
                    .show(requireActivity().supportFragmentManager, "Asset Selection Dialog")
            } else {
                TargetSelectionDialogFragment(true, this).show(requireActivity().supportFragmentManager, "Target Selection Dialog")
            }
        }

        toCard.setOnClickListener {
            if (transactionType==TransactionType.INCOME || transactionType==TransactionType.TRANSFER) {
                AssetSelectionDialogFragment(false, AustromApplication.activeAssets, this)
                    .show(requireActivity().supportFragmentManager, "Asset Selection Dialog")
            } else {
                TargetSelectionDialogFragment(false , this).show(requireActivity().supportFragmentManager, "Target Selection Dialog")
            }
        }

        submitButton.setOnClickListener {
            val provider = FirebaseDatabaseProvider(requireActivity())
            val categoryChip : Chip = view.findViewById(categoryChips.checkedChipId)
            val dateChip : Chip = view.findViewById(dateChips.checkedChipId)
            val dateInt = provider.parseDateToIntDate(dateChip.tag as LocalDate)
            if (sourceName!=null && targetName!=null) {
                val currencyCode = if (transactionType == TransactionType.INCOME) {
                    selectedTarget?.currencyCode
                } else {
                    selectedSource?.currencyCode
                }
                val secondaryAmount = if (sumReceivedText.visibility == View.VISIBLE) {
                    sumReceivedText.text.toString().toDouble()
                } else {
                    null
                }
                provider.writeNewTransaction(Transaction(
                    userId = AustromApplication.appUser?.userId,
                    sourceId = selectedSource?.assetId,
                    sourceName = sourceName,
                    targetId = selectedTarget?.assetId,
                    targetName = targetName,
                    amount = sumText.text.toString().toDouble(),
                    secondaryAmount = secondaryAmount,
                    categoryId = categoryChip.text.toString(),
                    transactionDate = null,
                    transactionDateInt = dateInt,
                    comment = null
                ))
                if (selectedSource!=null) {
                    selectedSource!!.amount -= sumText.text.toString().toDouble()
                    provider.updateAsset(selectedSource!!)
                }
                if (selectedTarget!=null) {
                    if (sumReceivedText.visibility == View.VISIBLE) {
                        selectedTarget!!.amount += sumReceivedText.text.toString().toDouble()
                    } else {
                        selectedTarget!!.amount += sumText.text.toString().toDouble()
                    }
                    provider.updateAsset(selectedTarget!!)
                }
                parentDialog.updateTransactionsList()
            }
            dismiss()
        }

        calendarButton.setOnClickListener {
            MaterialDatePicker.Builder.datePicker().setTitleText("Choose Transaction Date").build().show(requireActivity().supportFragmentManager, "DatePicker Dialog")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_transaction_creation, container, false)
    }

    fun receiveTargetSelection(asset: Asset?, name: String?) {
        if (asset!=null) {
            selectedTarget = asset
            targetName = asset.assetName
            toName.text = asset.assetName
            if (transactionType==TransactionType.INCOME) {
                currencySymbol.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
            }
        } else {
            selectedTarget = null
            targetName = name
            toName.text = name
        }
        switchSecondaryCurrencyVisibility()
    }

    fun receiveSourceSelection(asset: Asset?, name: String?) {
        if (asset!=null) {
            selectedSource = asset
            sourceName = asset.assetName
            fromName.text = asset.assetName
            if (transactionType==TransactionType.EXPENSE) {
                currencySymbol.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
            }
        } else {
            selectedSource = null
            sourceName = name
            fromName.text = name
        }
        switchSecondaryCurrencyVisibility()
    }

    private fun switchSecondaryCurrencyVisibility() {
        if(selectedSource!=null && selectedTarget!=null && selectedTarget!!.currencyCode!=selectedSource!!.currencyCode) {
            sumReceivedText.visibility = View.VISIBLE
            currencySymbolReceived.visibility = View.VISIBLE
            currencySymbolReceived.text = AustromApplication.activeCurrencies[selectedTarget?.currencyCode]?.symbol
            currencySymbol.text = AustromApplication.activeCurrencies[selectedSource?.currencyCode]?.symbol
        } else {
            sumReceivedText.visibility = View.GONE
            currencySymbolReceived.visibility = View.GONE
        }
    }

    private fun setUpCategoriesInChips() {

        val categories = when (transactionType) {
            TransactionType.TRANSFER -> Category.defaultTransferCategories
            TransactionType.INCOME -> Category.defaultIncomeCategories
            TransactionType.EXPENSE -> Category.defaultExpenseCategories
        }
        for (category in categories) {
            val chip = Chip(requireActivity())
            chip.text = category.name
            chip.chipIcon = ContextCompat.getDrawable(requireActivity(), category.imgReference ?: R.drawable.ic_placeholder_icon)
            chip.setEnsureMinTouchTargetSize(false)
            chip.isCheckable = true
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

    private fun setUpPrimaryAsset() {
        val primaryAssetId = AustromApplication.appUser?.primaryPaymentMethod
        if (primaryAssetId!=null && AustromApplication.activeAssets[primaryAssetId]!=null) {
            val primaryAsset = AustromApplication.activeAssets[primaryAssetId]
            if (transactionType==TransactionType.INCOME) {
                receiveTargetSelection(primaryAsset, null)
            } else {
                receiveSourceSelection(primaryAsset, null)
            }
        }
    }

    private fun bindViews(view: View) {
        sumText = view.findViewById(R.id.ctdial_sum_txt)
        sumReceivedText = view.findViewById(R.id.ctdial_sumReceived_txt)
        fromCard = view.findViewById(R.id.ctdial_cardFrom_crv)
        fromName = view.findViewById(R.id.ctdial_fromName_txt)
        toCard = view.findViewById(R.id.ctdial_cardTo_crv)
        toName = view.findViewById(R.id.ctdial_toName_txt)
        categoryChips = view.findViewById(R.id.ctdial_categoriesChips_cgr)
        dateChips = view.findViewById(R.id.ctdial_datesChips_cgr)
        submitButton = view.findViewById(R.id.ctdial_submit_btn)
        calendarButton = view.findViewById(R.id.ctdial_openCalendar_btn)
        currencySymbol = view.findViewById(R.id.ctdial_currencySymbol_txt)
        currencySymbolReceived = view.findViewById(R.id.ctdial_currencyReceived_txt)
    }
}