package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.BudgetCreationDialogFragment

class SharedBudgetFragment : Fragment(R.layout.fragment_shared_budget) {
    private lateinit var addNewBudgetButton: Button


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        addNewBudgetButton.setOnClickListener {
            BudgetCreationDialogFragment().show(requireActivity().supportFragmentManager, "Budget Creation Dialog")
        }
    }

    private fun bindViews(view: View) {
        addNewBudgetButton = view.findViewById(R.id.shb_createNewBudget_btn)
    }
}