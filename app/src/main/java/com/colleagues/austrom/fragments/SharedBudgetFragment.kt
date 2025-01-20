package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.models.Budget

class SharedBudgetFragment(private val activeBudget: Budget) : Fragment(R.layout.fragment_shared_budget) {
    //region Binding
    private lateinit var budgetName: TextView
    private lateinit var budgetInviteCode: TextView
    private lateinit var leaveButton: Button
    private fun bindViews(view: View) {
        budgetName = view.findViewById(R.id.shb_budgetName_txt)
        budgetInviteCode = view.findViewById(R.id.shb_invitationCode_txt)
        leaveButton = view.findViewById(R.id.shb_leaveBudget_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        budgetName.text = activeBudget.budgetName
        budgetInviteCode.text = AustromApplication.appUser!!.tokenId

        leaveButton.setOnClickListener { leaveBudget() }
    }

    private fun leaveBudget() {
        activeBudget.leave(FirebaseDatabaseProvider(requireActivity()))
        (requireActivity() as MainActivity).switchFragment(SharedBudgetEmptyFragment())
    }
}