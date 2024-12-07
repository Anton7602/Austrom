package com.colleagues.austrom.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.InvalidTransactionException
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
    private lateinit var dialogHolder: CardView
    private var selectedSource : Asset? = null
    private var sourceName: String? = null
    private var selectedTarget : Asset? = null
    private var targetName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
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
            val dbProvider = LocalDatabaseProvider(requireActivity())
            val categoryChip : Chip = view.findViewById(categoryChips.checkedChipId)
            val dateChip : Chip = view.findViewById(dateChips.checkedChipId)
            if (sourceName!=null && targetName!=null) {
                try{
                    when (transactionType) {
                        TransactionType.EXPENSE -> Transaction(
                            amount = -sumText.text.toString().toDouble(),
                            assetId = selectedSource!!.assetId,
                            categoryId = categoryChip.text.toString(),
                            transactionName = targetName.toString(),
                            transactionDate = (dateChip.tag as LocalDate)).submit(dbProvider)
                        TransactionType.INCOME -> Transaction(
                            amount = sumText.text.toString().toDouble(),
                            assetId = selectedTarget!!.assetId,
                            categoryId = categoryChip.text.toString(),
                            transactionName = sourceName.toString(),
                            transactionDate = (dateChip.tag as LocalDate)).submit(dbProvider)
                        TransactionType.TRANSFER -> {
                            val transactionOut = Transaction(
                                amount = -sumText.text.toString().toDouble(),
                                assetId = selectedSource!!.assetId,
                                categoryId = categoryChip.text.toString(),
                                transactionName = targetName.toString(),
                                transactionDate = (dateChip.tag as LocalDate))
                            val transactionIn = Transaction(
                                amount = if (sumReceivedText.visibility == View.VISIBLE) sumReceivedText.text.toString().toDouble() else sumText.text.toString().toDouble(),
                                assetId = selectedTarget!!.assetId,
                                categoryId = categoryChip.text.toString(),
                                transactionName = sourceName.toString(),
                                transactionDate = (dateChip.tag as LocalDate))
                            transactionOut.linkTo(transactionIn)
                            transactionIn.submit(dbProvider)
                            transactionOut.submit(dbProvider)
                        }
                    }
                    parentDialog.updateTransactionsList()
                    dismiss()
                } catch (ex: InvalidTransactionException) {
                    Toast.makeText(requireActivity(), ex.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        calendarButton.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Choose Transaction Date").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                for (chipView in dateChips.children) {
                    val chip = chipView as Chip
                    if ((chip.tag as LocalDate) == selectedDate) {
                        chip.isChecked = true
                        dateChips.getChildAt(0).visibility = View.GONE
                        return@addOnPositiveButtonClickListener
                    }
                }
                val customChip = dateChips.getChildAt(0) as Chip
                customChip.visibility = View.VISIBLE
                customChip.text = selectedDate.toDayOfWeekAndShortDateFormat()
                customChip.tag = selectedDate
                customChip.isChecked = true
            }
            datePicker.show(requireActivity().supportFragmentManager, "DatePicker Dialog")
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
            //TransactionType.TRANSFER -> AustromApplication.getActiveTransferCategories()
            //TransactionType.INCOME -> AustromApplication.getActiveIncomeCategories()
            //TransactionType.EXPENSE -> AustromApplication.getActiveExpenseCategories()

            TransactionType.TRANSFER -> Category.defaultTransferCategories
            TransactionType.INCOME -> Category.defaultIncomeCategories
            TransactionType.EXPENSE -> Category.defaultExpenseCategories
        }
        for (category in categories) {
            val chip = Chip(requireActivity())
            chip.text = category.name
            chip.chipIcon = ContextCompat.getDrawable(requireActivity(), category.imgReference?.resourceId ?: R.drawable.ic_placeholder_icon)
            chip.setEnsureMinTouchTargetSize(false)
            chip.isCheckable = true
            if (category == categories[0]) {
                chip.isChecked = true
            }
            categoryChips.addView(chip)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDateChips() {
        var chipDate = LocalDate.now()
        val customDateChip = Chip(requireActivity())
        customDateChip.text = "Custom date"
        customDateChip.tag = chipDate
        customDateChip.setEnsureMinTouchTargetSize(false)
        customDateChip.isCheckable = true
        customDateChip.visibility = View.GONE
        dateChips.addView(customDateChip)
        for (i in 0..9) {
            val chip = Chip(requireActivity())
            chip.text = chipDate.toDayOfWeekAndShortDateFormat()
            chip.tag = chipDate
            chip.setEnsureMinTouchTargetSize(false)
            chip.isCheckable = true
            if (i == 0) {
                chip.isChecked = true
            }
            dateChips.addView(chip)
            chipDate = chipDate.minusDays(1)
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
        dialogHolder = view.findViewById(R.id.ctdial_holder_crv)
    }
}