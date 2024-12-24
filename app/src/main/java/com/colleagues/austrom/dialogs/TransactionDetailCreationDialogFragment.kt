package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionPropertiesActivity
import com.colleagues.austrom.models.QuantityUnit
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType


class TransactionDetailCreationDialogFragment(private var parent: TransactionPropertiesActivity,  private var transaction: Transaction) : Fragment(R.layout.dialog_fragment_transaction_detail_creation) {
    //region Binding
    private lateinit var addRemoveButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var constructorLabel: TextView
    private lateinit var detailNameField: EditText
    private lateinit var quantityField: EditText
    private lateinit var quantityTypeSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var costField: EditText
    private lateinit var currencyLabel: TextView
    private fun bindViews(view: View) {
        addRemoveButton = view.findViewById(R.id.trdetcr_addRemove_btn)
        forwardButton = view.findViewById(R.id.trdetcr_forward_btn)
        constructorLabel = view.findViewById(R.id.trdetcr_buttonLabel_txt)
        detailNameField = view.findViewById(R.id.trdetcr_detailName_txt)
        quantityField = view.findViewById(R.id.trdetcr_detailQuantity_txt)
        quantityTypeSpinner = view.findViewById(R.id.trdetcr_quantityType_spr)
        detailNameField = view.findViewById(R.id.trdetcr_detailName_txt)
        costField = view.findViewById(R.id.trdetcr_cost_txt)
        currencyLabel = view.findViewById(R.id.trdetcr_currency_txt)
        categorySpinner = view.findViewById(R.id.trdet_category_spr)
    }
    //endregion

    private var currentStageId = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpSpinners()
        setStage(0)

        addRemoveButton.setOnClickListener { if (currentStageId==0) setStage(1) else setStage(0) }
        constructorLabel.setOnClickListener{ setStage(1) }
        forwardButton.setOnClickListener { moveToNextStage() }
        costField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateUnallocatedSum() }
        })
    }

    //Stages of TransactionDetailConstruction:
    //0 - No active fields visible, only label "Add New Entry"
    //1 - Item Name Field active, button forward have icon "Forward"
    //2 - Item Quantity and Quantity Types fields active, button forward have icon "Forward"
    //3 - Category Field active, forward button have icon forward
    //4 - Cost Field Active, button forward have icon "Done"
    private fun setStage(stageId: Int) {
        currentStageId = stageId
        addRemoveButton.setImageResource(if (stageId==0) R.drawable.ic_navigation_add_temp else R.drawable.ic_navigation_close_temp)
        constructorLabel.visibility = if (stageId==0) View.VISIBLE else View.GONE
        forwardButton.visibility = if (stageId!=0) View.VISIBLE else View.GONE
        forwardButton.setImageResource(if (stageId==4) R.drawable.ic_navigation_done_temp else R.drawable.ic_navigation_forward_temp)
        detailNameField.visibility = if (stageId==1) View.VISIBLE else View.GONE
        quantityField.visibility = if (stageId==2) View.VISIBLE else View.GONE
        quantityTypeSpinner.visibility = if (stageId==2) View.VISIBLE else View.GONE
        categorySpinner.visibility = if (stageId==3) View.VISIBLE else View.GONE
        costField.visibility = if (stageId==4) View.VISIBLE else View.GONE
        currencyLabel.visibility = if (stageId==4) View.VISIBLE else View.GONE
        currencyLabel.text = AustromApplication.activeCurrencies[AustromApplication.activeAssets[transaction.assetId]?.currencyCode]?.symbol
        AustromApplication.showKeyboard(requireActivity(), when (stageId) {
            1 -> detailNameField
            2 -> quantityField
            4 -> costField
            else -> detailNameField
        })
    }

    private fun moveToNextStage() {
        if (currentStageId==4) {
            if (costField.text.toString().isNotEmpty()) {
                if (parent.updateUnallocatedSum(costField.text.toString().toDouble())>=0) {
                    parent.addTransactionDetail(TransactionDetail(
                        transactionId = transaction.transactionId,
                        name = detailNameField.text.toString(),
                        quantity = if (quantityField.text.toString().isEmpty()) null else quantityField.text.toString().toDouble(),
                        typeOfQuantity = if (quantityField.text.toString().isEmpty()) null else quantityTypeSpinner.selectedItem as QuantityUnit,
                        cost = costField.text.toString().toDouble(),
                        categoryName = if (categorySpinner.selectedItem.toString()!=transaction.categoryId) categorySpinner.selectedItem.toString() else null
                    ))
                    setStage(0)
                    clearValues()
                } else {
                    //TODO(Error message. Transaction balance is negative)
                }
            } else {
                //TODO(Error message Cost Field is Empty)
            }
        } else {
            setStage(currentStageId+1)
        }
    }

    private fun updateUnallocatedSum() {
        if (costField.text.isNotEmpty()) {
            parent.updateUnallocatedSum(costField.text.toString().toDouble())
        } else {
            parent.updateUnallocatedSum()
        }
    }

    private fun clearValues() {
        detailNameField.text.clear()
        quantityField.text.clear()
        costField.text.clear()
    }

    private fun setUpSpinners() {
        val quantityAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, QuantityUnit.entries.toTypedArray())
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        quantityTypeSpinner.adapter = quantityAdapter
        quantityTypeSpinner.setSelection(0)

        val availableCategories = when (transaction.transactionType()) {
            TransactionType.EXPENSE -> AustromApplication.activeExpenseCategories.values.toList()
            TransactionType.INCOME ->  AustromApplication.activeIncomeCategories.values.toList()
            TransactionType.TRANSFER ->  AustromApplication.activeTransferCategories.values.toList()
        }
        val categoryAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, availableCategories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        categorySpinner.setSelection(availableCategories.indexOf(availableCategories.find { entry -> entry.name == transaction.categoryId }))
    }
}