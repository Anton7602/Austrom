package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.Budget
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class BudgetCreationDialogFragment : BottomSheetDialogFragment() {
    private lateinit var submitNewBudget: Button
    private lateinit var budgetNameTextView: TextInputEditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        budgetNameTextView.requestFocus()

        submitNewBudget.setOnClickListener {
            val provider : IDatabaseProvider = FirebaseDatabaseProvider()
            provider.writeNewBudget(Budget(budgetNameTextView.text.toString(),
                arrayListOf((requireActivity().application as AustromApplication).appUser?.userID.toString())
            ))
            this.dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_budget_creation, container, false)
    }

    private fun bindViews(view: View) {
        submitNewBudget = view.findViewById(R.id.bcdial_submit_btn)
        budgetNameTextView = view.findViewById(R.id.bcdial_budgetName_txt)
    }
}