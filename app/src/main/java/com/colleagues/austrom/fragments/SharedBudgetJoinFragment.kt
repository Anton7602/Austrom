package com.colleagues.austrom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.TextEditDialogFragment
import com.colleagues.austrom.models.Budget
import com.google.android.material.textfield.TextInputEditText

class SharedBudgetJoinFragment(private val budget: Budget) : Fragment(R.layout.fragment_shared_budget_join) {
    //region Binding
    private lateinit var budgetNameTextView: TextView
    private lateinit var invitationCodeTextView: TextInputEditText
    private lateinit var joinButton: Button
    private fun bindViews(view: View) {
        budgetNameTextView = view.findViewById(R.id.budjoin_budgetName_txt)
        invitationCodeTextView = view.findViewById(R.id.budjoin_invitationCode_txt)
        joinButton = view.findViewById(R.id.budjoin_joinBudget_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        budgetNameTextView.text = budget.budgetName


        joinButton.setOnClickListener { acceptInvitationWithCode(invitationCodeTextView.text.toString()) }
    }

    private fun acceptInvitationWithCode(invitationCode: String) {

    }
}