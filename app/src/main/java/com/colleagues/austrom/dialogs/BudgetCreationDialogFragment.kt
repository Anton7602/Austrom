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
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.fragments.SharedBudgetFragment
import com.colleagues.austrom.models.Budget
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class BudgetCreationDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_budget_creation, container, false) }
    fun setOnDialogResultListener(l: (()->Unit)) { returnResult = l }
    private var returnResult: ()->Unit = {}
    //region Binding
    private lateinit var submitNewBudget: Button
    private lateinit var budgetNameTextView: TextInputEditText
    private lateinit var dialogHolder: CardView
    private fun bindViews(view: View) {
        submitNewBudget = view.findViewById(R.id.bcdial_submit_btn)
        budgetNameTextView = view.findViewById(R.id.bcdial_budgetName_txt)
        dialogHolder = view.findViewById(R.id.bcdial_holder_crd)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        budgetNameTextView.requestFocus()
        submitNewBudget.setOnClickListener { submitNewBudget(); this.dismiss() }
    }

    private fun submitNewBudget() {
        val budgetOwner = AustromApplication.appUser
        if (budgetOwner!=null) {
            val newBudget = Budget.createNewBudget(budgetOwner, budgetNameTextView.text.toString(), LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity()))
            if (budgetOwner.activeBudgetId!=null) {
                (requireActivity() as MainActivity).switchFragment(SharedBudgetFragment(newBudget))
            } else {
                //TODO("Something Went Horribly Wrong. React to it!!")
            }
        }
    }

}