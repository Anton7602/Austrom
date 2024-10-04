package com.colleagues.austrom.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionPropertiesActivity
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.QuantityUnit
import com.colleagues.austrom.models.TransactionDetail


class TransactionDetailCreationDialogFragment(private var parent: TransactionPropertiesActivity) : Fragment(R.layout.dialog_fragment_transaction_detail_creation) {
    private lateinit var addRemoveButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var constructorLabel: TextView
    private lateinit var detailNameField: EditText
    private lateinit var quantityField: EditText
    private lateinit var quantityTypeSpinner: Spinner
    private lateinit var costField: EditText
    private lateinit var currencyLabel: TextView
    private var currentStageId = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpSpinner()
        setStage(0)

        addRemoveButton.setOnClickListener { if (currentStageId==0) setStage(1) else setStage(0) }
        constructorLabel.setOnClickListener{ setStage(1) }
        forwardButton.setOnClickListener {
            if (currentStageId==3) {
                parent.addTransactionDetail(TransactionDetail(
                    name = detailNameField.text.toString(),
                    quantity = if (quantityField.text.toString().isEmpty()) null else quantityField.text.toString().toDouble(),
                    typeOfQuantity = if (quantityField.text.toString().isEmpty()) null else quantityTypeSpinner.selectedItem as QuantityUnit,
                    cost = costField.text.toString().toDouble(),
                    category = Category.defaultIncomeCategories[0]
                ))
                setStage(0)
                clearValues()
            } else {
                setStage(currentStageId+1)
            }
        }
    }

    //Stages of TransactionDetailConstruction:
    //0 - No active fields visible, only label "Add New Entry"
    //1 - Item Name Field active, button forward have icon "Forward"
    //2 - Item Quantity and Quantity Types fields active, button forward have icon "Forward"
    //3 - Cost Field Active, button forward have icon "Done"
    private fun setStage(stageId: Int) {
        currentStageId = stageId
        addRemoveButton.setImageResource(if (stageId==0) R.drawable.ic_navigation_add_temp else R.drawable.ic_navigation_close_temp)
        constructorLabel.visibility = if (stageId==0) View.VISIBLE else View.GONE
        forwardButton.visibility = if (stageId!=0) View.VISIBLE else View.GONE
        forwardButton.setImageResource(if (stageId==3) R.drawable.ic_navigation_done_temp else R.drawable.ic_navigation_forward_temp)
        detailNameField.visibility = if (stageId==1) View.VISIBLE else View.GONE
        quantityField.visibility = if (stageId==2) View.VISIBLE else View.GONE
        quantityTypeSpinner.visibility = if (stageId==2) View.VISIBLE else View.GONE
        costField.visibility = if (stageId==3) View.VISIBLE else View.GONE
        currencyLabel.visibility = if (stageId==3) View.VISIBLE else View.GONE
        when (stageId) {
            1 -> (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(detailNameField, InputMethodManager.SHOW_IMPLICIT)
            2 -> (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(quantityField, InputMethodManager.SHOW_IMPLICIT)
            3 -> (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(costField, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun clearValues() {
        detailNameField.text.clear()
        quantityField.text.clear()
        costField.text.clear()
    }

    private fun setUpSpinner() {
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, QuantityUnit.entries.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        quantityTypeSpinner.adapter = adapter
        quantityTypeSpinner.setSelection(0)
    }

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
    }
}