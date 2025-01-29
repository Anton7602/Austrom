package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.BudgetCreationDialogFragment
import com.colleagues.austrom.dialogs.TextEditDialogFragment
import com.colleagues.austrom.models.Budget

class SharedBudgetEmptyFragment : Fragment(R.layout.fragment_shared_budget_empty) {
    //region Binding
    private lateinit var createNewBudgetButton: Button
    private fun bindViews(view: View) {
        createNewBudgetButton = view.findViewById(R.id.shbe_createBudget_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        createNewBudgetButton.setOnClickListener { launchBudgetCreationDialog() }
    }

    private fun launchBudgetCreationDialog() {
        val dialog = TextEditDialogFragment(null, getString(R.string.budget_name))
        dialog.setOnDialogResultListener { budgetName -> createNewBudget(budgetName) }
        dialog.show(requireActivity().supportFragmentManager, "AssetTypeSelectionDialog")
    }


    private fun createNewBudget(budgetName: String) {
        val budget = Budget.createNewBudget(AustromApplication.appUser!!, budgetName, LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity()))
        (requireActivity() as MainActivity).switchFragment(SharedBudgetFragment(budget))
    }
}