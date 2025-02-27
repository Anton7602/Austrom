package com.colleagues.austrom.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.AustromApplication.Companion.appUser
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.managers.SyncManager
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Invitation
import com.google.android.material.textfield.TextInputEditText

class SharedBudgetJoinFragment(private val budget: Budget) : Fragment(R.layout.fragment_shared_budget_join) {
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var budgetNameTextView: TextView
    private lateinit var budgetChangeWarningText: TextView
    private lateinit var invitationCodeTextView: TextInputEditText
    private lateinit var joinButton: Button
    private lateinit var callNavDrawerButton: ImageButton
    private fun bindViews(view: View) {
        budgetNameTextView = view.findViewById(R.id.budjoin_budgetName_txt)
        invitationCodeTextView = view.findViewById(R.id.budjoin_invitationCode_txt)
        joinButton = view.findViewById(R.id.budjoin_joinBudget_btn)
        callNavDrawerButton = view.findViewById(R.id.budjoin_navDrawer_btn)
        budgetChangeWarningText = view.findViewById(R.id.budjoin_warningBudgetChange_txt)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        budgetNameTextView.text = budget.budgetName

        if (appUser!!.activeBudgetId!=null) {
            val remoteDBProvider = FirebaseDatabaseProvider(requireActivity())
            val budget = remoteDBProvider.getBudgetById(appUser!!.activeBudgetId.toString())
            if (budget!=null) {
                var warningText = getString(R.string.you_re_already_a_part_of, budget.budgetName) + "\n" + getString(R.string.if_you_accept_this_invitation_you_will_leave_the_budget_you_are_currently_in)
                budgetChangeWarningText.text = warningText
                budgetChangeWarningText.visibility = View.VISIBLE
            }
        }

        callNavDrawerButton.setOnClickListener { requestNavigationDrawerOpen() }
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
        if (invitation!=null && appUser!!.userId==(invitation.userId)) {
            budget.addUser(appUser!!, invitation.token, LocalDatabaseProvider(requireActivity()), remoteDBProvider)
            remoteDBProvider.deleteInvitationToUser(appUser!!, budget)
            synchronizeWithBudget()
            (requireActivity() as MainActivity).switchFragment(SharedBudgetFragment(budget))
        } else {
            Toast.makeText(requireActivity(), getString(R.string.invalid_invitation_code), Toast.LENGTH_LONG).show()
        }
    }

    private fun synchronizeWithBudget() {
        if (appUser?.activeBudgetId!=null) {
            val remoteDBProvider = FirebaseDatabaseProvider(requireActivity())
            val currentBudget = remoteDBProvider.getBudgetById(appUser!!.activeBudgetId!!)
            if (currentBudget!=null) {
                SyncManager(requireActivity(), LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity())).sync()
            }
        }
    }
}