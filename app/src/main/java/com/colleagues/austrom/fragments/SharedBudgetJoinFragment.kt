package com.colleagues.austrom.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.colleagues.austrom.AustromApplication.Companion.appUser
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Invitation
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
        val remoteDBProvider = FirebaseDatabaseProvider(requireActivity())
        var invitation: Invitation? = null
        try {
            invitation = remoteDBProvider.acceptInvitation(appUser!!, budget, invitationCode)
        } catch(e: Exception) {
            Log.d("Debug", e.message.toString())
        }
        val users = remoteDBProvider.getUsersByBudget(budget.budgetId)
        if (users.containsKey(invitation?.userId)) {
            appUser!!.activeBudgetId = budget.budgetId
            appUser!!.tokenId = invitation?.token
            //LocalDatabaseProvider(requireActivity()).updateUser(appUser!!)
        }
        val test = 5

    }
}