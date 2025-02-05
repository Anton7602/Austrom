package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Spinner
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.QuantityUnit
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.absoluteValue


class TransactionDetailCreationNewDialogFragment(private val transaction: Transaction, private val transactionDetails: List<TransactionDetail>) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_transaction_detail_creation, container, false) }
    fun setOnDialogResultListener(l: ((TransactionDetail?)->Unit)) { returnResult = l }
    private var returnResult: (TransactionDetail?)->Unit = {}
    fun setOnDetailChangedListener(l: ((name: String, quantity: Double, unit: String, amount: Double)->Unit)) { returnDetailValues = l }
    private var returnDetailValues: (String, Double, String, Double) -> Unit = { _, _, _, _ -> }
    //region Binding
    private lateinit var dialogHolder: CardView
    private lateinit var quantityTypeSpinner: Spinner
    private lateinit var detailNameTextView: AutoCompleteTextView
    private lateinit var detailNameLayout: TextInputLayout
    private lateinit var quantityTextView: TextInputEditText
    private lateinit var quantityLayout: ConstraintLayout
    private lateinit var amountTextView: TextInputEditText
    private lateinit var amountLayout: TextInputLayout
    private lateinit var nextButton: ImageButton
    private fun bindViews(view: View) {
        detailNameLayout = view.findViewById(R.id.trdetcreat2_detailName_til)
        quantityLayout = view.findViewById(R.id.trdetcreat2_quantityLayout_cly)
        amountLayout = view.findViewById(R.id.trdetcreat2_amount_til)

        dialogHolder = view.findViewById(R.id.trdetcreat2_dialogHolder_crv)
        detailNameTextView = view.findViewById(R.id.trdetcreat2_detailName_txt)
        quantityTextView = view.findViewById(R.id.trdetcreat2_quantity_txt)
        quantityTypeSpinner = view.findViewById(R.id.trdetcreat2_quantityType_spr)
        amountTextView = view.findViewById(R.id.trdetcreat2_amount_txt)
        nextButton = view.findViewById(R.id.trdetcreat2_next_btn)
    }
    // endregion
    private var currentStageId = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        setUpAutofill()
        setUpSpinners()
        amountTextView.setHint((transaction.amount.absoluteValue-transaction.sumOfTransactionDetailsAmounts(LocalDatabaseProvider(requireActivity()))).absoluteValue.toMoneyFormat())
        detailNameTextView.addTextChangedListener { _ -> returnDetailValues(getValidatedDetailName(), getValidatedQuantity(), quantityTypeSpinner.selectedItem.toString(), getValidatedAmount()) }
        quantityTextView.addTextChangedListener { _ -> returnDetailValues(getValidatedDetailName(), getValidatedQuantity(), quantityTypeSpinner.selectedItem.toString(), getValidatedAmount())  }
        amountTextView.addTextChangedListener { _ -> returnDetailValues(getValidatedDetailName(), getValidatedQuantity(), quantityTypeSpinner.selectedItem.toString(), getValidatedAmount())  }
        quantityTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) { returnDetailValues(getValidatedDetailName(), getValidatedQuantity(), quantityTypeSpinner.selectedItem.toString(), getValidatedAmount())  }
            override fun onNothingSelected(parent: AdapterView<*>) { returnDetailValues(getValidatedDetailName(), getValidatedQuantity(), quantityTypeSpinner.selectedItem.toString(), getValidatedAmount()) }
        }
        detailNameTextView.setOnKeyListener { _, keyCode, event -> if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) { moveToNextStage(); true} else false }
        quantityTextView.setOnKeyListener { _, keyCode, event -> if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) { moveToNextStage(); true} else false }
        amountTextView.setOnKeyListener { _, keyCode, event ->if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) { moveToNextStage(); true} else false }
        nextButton.setOnClickListener { moveToNextStage() }
        returnDetailValues(getValidatedDetailName(), getValidatedQuantity(), quantityTypeSpinner.selectedItem.toString(), getValidatedAmount())
        setStage(0)
    }

    private fun getValidatedDetailName(): String {
        return if (!detailNameTextView.text.isNullOrEmpty()) {
            detailNameTextView.text.toString()
        } else {
            getString(R.string.unallocated_balance)
        }
    }

    private fun getValidatedQuantity(): Double {
        quantityTextView.error = null
        if (quantityTextView.text.isNullOrEmpty()) return 0.0
        val providedValue = quantityTextView.text.toString().parseToDouble()
        if (providedValue == null) { quantityTextView.error = "Provided Invalid Value"; return 0.0 }
        if (providedValue < 0) {quantityTextView.error = "Quantity Cannot Be Negative"; return 0.0}
        if (providedValue>Double.MAX_VALUE) {quantityTextView.error = "Quantity Exceeds Maximum Allowed Value"; return Double.MAX_VALUE}
        return quantityTextView.text.toString().toDouble()
    }

    private fun getValidatedAmount(): Double {
        val maxAllowedValue = transaction.amount.absoluteValue-transactionDetails.sumOf { it.cost }
        amountTextView.error = null
        if (amountTextView.text.isNullOrEmpty()) return maxAllowedValue
        val providedValue = amountTextView.text.toString().parseToDouble()
        if (providedValue==null) { amountTextView.error = "Provided Invalid Value"; return 0.0 }
        if (providedValue<0) { amountTextView.error = "Value cannot be less than zero"; return 0.0 }
        if (providedValue>maxAllowedValue) {amountTextView.error = "Provided Value Overflows This Transaction"; return maxAllowedValue}
        return providedValue
    }

    private fun setStage(stageId: Int) {
        currentStageId = stageId
        detailNameLayout.visibility = if (stageId==0) View.VISIBLE else View.GONE
        quantityLayout.visibility = if (stageId==1) View.VISIBLE else View.GONE
        amountLayout.visibility = if (stageId==2) View.VISIBLE else View.GONE
        when (stageId) {
            0 -> AustromApplication.showKeyboard(requireActivity(), detailNameTextView)
            1 -> AustromApplication.showKeyboard(requireActivity(), quantityTextView)
            2 -> AustromApplication.showKeyboard(requireActivity(), amountTextView)
        }
    }

    private fun moveToNextStage() {
        if (currentStageId==2) {
            createTransactionDetail()
        } else {
            setStage(currentStageId+1)
        }
    }

    private fun createTransactionDetail() {
        transaction.addDetail(TransactionDetail(
            transactionId = transaction.transactionId,
            name = getValidatedDetailName(),
            quantity = if (getValidatedQuantity() == 0.0) null else getValidatedQuantity(),
            typeOfQuantity = if (quantityTextView.text.toString().isEmpty()) null else QuantityUnit.entries[quantityTypeSpinner.selectedItemPosition],
            cost = getValidatedAmount(),
            categoryName = transaction.categoryId
        ), LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity()))
        this.dismiss()
    }

    private fun setUpSpinners() {
        val quantityAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, QuantityUnit.entries.toList().map {it -> getString(it.shortNameResourceId)})
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        quantityTypeSpinner.adapter = quantityAdapter
        if (transactionDetails.isNotEmpty()) {
            quantityTypeSpinner.setSelection(transactionDetails.last().typeOfQuantity?.ordinal ?: 0)
        } else {
            quantityTypeSpinner.setSelection(0)
        }

//        val availableCategories = when (transaction.transactionType()) {
//            TransactionType.EXPENSE -> AustromApplication.activeExpenseCategories.values.toList()
//            TransactionType.INCOME ->  AustromApplication.activeIncomeCategories.values.toList()
//            TransactionType.TRANSFER ->  AustromApplication.activeTransferCategories.values.toList()
//        }
//        val categoryAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, availableCategories)
//        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        categorySpinner.adapter = categoryAdapter
//        categorySpinner.setSelection(availableCategories.indexOf(availableCategories.find { entry -> entry.name == transaction.categoryId }))
    }

    private fun setUpAutofill() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        val activity = requireActivity()
        localDBProvider.getUniqueTransactionDetailsNamesAsync().observe(requireActivity()) {transactionDetailsName ->
            val adapter = ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, transactionDetailsName)
            detailNameTextView.setAdapter(adapter)
            detailNameTextView.threshold = 2
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        returnResult(null)
    }
}