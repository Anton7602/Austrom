package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.AustromApplication.Companion.appUser
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.ReceivedInvitationRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.TextEditDialogFragment
import com.colleagues.austrom.models.Budget

class SharedBudgetEmptyFragment : Fragment(R.layout.fragment_shared_budget_empty) {
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var createNewBudgetButton: Button
    private lateinit var callNavDrawerButton: ImageButton
    private lateinit var invitationRecommendationTextView: TextView
    private lateinit var invitationHolder: RecyclerView
    private fun bindViews(view: View) {
        createNewBudgetButton = view.findViewById(R.id.shbe_createBudget_btn)
        callNavDrawerButton = view.findViewById(R.id.shbe_navDrawer_btn)
        invitationRecommendationTextView = view.findViewById(R.id.shbe_inviteAdvice_txt)
        invitationHolder = view.findViewById(R.id.shbe_availableInvitations_rcv)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        createNewBudgetButton.setOnClickListener { launchBudgetCreationDialog() }
        callNavDrawerButton.setOnClickListener { requestNavigationDrawerOpen() }
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        localDBProvider.getReceivedInvitationsOfUserAsync(appUser!!).observe(viewLifecycleOwner) { invitations ->
            val remoteDBProvider: IRemoteDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
            val budgets = mutableListOf<Budget>()
            invitations.forEach { invitation ->
                remoteDBProvider.getBudgetById(invitation.budgetId)?.let { budgets.add(it) }
            }
            setUpRecyclerViews(budgets)
        }
    }

    private fun launchBudgetCreationDialog() {
        val dialog = TextEditDialogFragment(null, getString(R.string.budget_name))
        dialog.setOnDialogResultListener { budgetName -> createNewBudget(budgetName) }
        dialog.show(requireActivity().supportFragmentManager, "AssetTypeSelectionDialog")
    }


    private fun createNewBudget(budgetName: String) {
        val budget = Budget.createNewBudget(appUser!!, budgetName, LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity()))
        (requireActivity() as MainActivity).switchFragment(SharedBudgetFragment(budget))
    }

    private fun setUpRecyclerViews(budgets: List<Budget>) {
        invitationRecommendationTextView.text = if (budgets.isNotEmpty()) { getString(R.string.join_one_of_the_following_budgets_you_ve_been_invited_to) } else { getString(R.string.ask_your_partner_to_invite_you_to_their_budget) }
        invitationHolder.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = ReceivedInvitationRecyclerAdapter(budgets)
        adapter.setOnItemAcceptClickListener { budget ->
            (requireActivity() as MainActivity).switchFragment(SharedBudgetJoinFragment(budget))
        }
        adapter.setOnItemDeclineClickListener { budget ->
            budget.recallInvitationToUser(appUser!!, LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity()))
        }
        invitationHolder.adapter = adapter
    }
}