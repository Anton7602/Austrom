package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.fragments.SharedBudgetFragment
import com.colleagues.austrom.models.Budget
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class BudgetCreationDialogFragment : BottomSheetDialogFragment() {
    private lateinit var submitNewBudget: Button
    private lateinit var budgetNameTextView: TextInputEditText
    private lateinit var dialogHolder: CardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        budgetNameTextView.requestFocus()

        submitNewBudget.setOnClickListener {
            val budgetCreator = AustromApplication.appUser
            if (budgetCreator!=null) {
                val provider : IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
                val newBudget = Budget(
                    budgetName =  budgetNameTextView.text.toString(),
                    users =  arrayListOf(budgetCreator.userId.toString())
                )
                newBudget.budgetId = provider.createNewBudget(newBudget)
                budgetCreator.activeBudgetId = newBudget.budgetId
                if (budgetCreator.activeBudgetId!=null) {
                    provider.updateUser(budgetCreator)
                    (requireActivity() as MainActivity).switchFragment(SharedBudgetFragment(newBudget))
                }
            }
            this.dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_budget_creation, container, false)
    }

    private fun bindViews(view: View) {
        submitNewBudget = view.findViewById(R.id.bcdial_submit_btn)
        budgetNameTextView = view.findViewById(R.id.bcdial_budgetName_txt)
        dialogHolder = view.findViewById(R.id.bcdial_holder_crd)
    }
}