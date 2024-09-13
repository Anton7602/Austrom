package com.colleagues.austrom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.BudgetCreationDialogFragment
import com.colleagues.austrom.dialogs.BudgetJoinDialogFragment

class SharedBudgetEmptyFragment : Fragment(R.layout.fragment_shared_budget_empty) {
    private lateinit var createNewBudgetButton: Button
    private lateinit var joinNewBudgetButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        createNewBudgetButton.setOnClickListener {
            BudgetCreationDialogFragment().show(requireActivity().supportFragmentManager, "Budget Creation Dialog")
        }

        joinNewBudgetButton.setOnClickListener {
            BudgetJoinDialogFragment().show(requireActivity().supportFragmentManager, "Budget Join Dialog")
        }
    }

    private fun bindViews(view: View) {
        createNewBudgetButton = view.findViewById(R.id.shbe_createBudget_btn)
        joinNewBudgetButton = view.findViewById(R.id.shbe_joinBudget_btn)
    }

}