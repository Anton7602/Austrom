package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.InvitationRecyclerAdapter
import com.colleagues.austrom.adapters.UserRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.InvitationSentDialogFragment
import com.colleagues.austrom.dialogs.bottomsheetdialogs.TextEditDialogFragment
import com.colleagues.austrom.models.Budget

class SharedBudgetFragment(private val activeBudget: Budget) : Fragment(R.layout.fragment_shared_budget) {
    //region Binding
    private lateinit var budgetName: TextView
    private lateinit var leaveButton: Button
    private lateinit var inviteButton: Button
    private lateinit var invitationsRecycler: RecyclerView
    private lateinit var usersRecycler: RecyclerView
    private fun bindViews(view: View) {
        budgetName = view.findViewById(R.id.shb_budgetName_txt)
        leaveButton = view.findViewById(R.id.shb_leaveBudget_btn)
        inviteButton = view.findViewById(R.id.shb_inviteUser_btn)
        invitationsRecycler = view.findViewById(R.id.shb_pendingInvitations_rcv)
        usersRecycler = view.findViewById(R.id.shb_activeUsers_rcv)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerViews()
        budgetName.text = activeBudget.budgetName

        leaveButton.setOnClickListener { leaveBudget() }
        inviteButton.setOnClickListener { launchInviteUserDialog() }
    }

    private fun launchInviteUserDialog() {
        val remoteDBProvider: IRemoteDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
        val dialog = TextEditDialogFragment(null, getString(R.string.email_of_invited_user))
        dialog.setOnDialogResultListener { emailOfUser ->
            val user = remoteDBProvider.getUserByEmail(emailOfUser)
            if (user!=null) {
                val invitation = activeBudget.inviteUser(user, LocalDatabaseProvider(requireActivity()), remoteDBProvider)
                val successDialog = InvitationSentDialogFragment(invitation.invitationCode)
                successDialog.setOnDialogResultListener { result ->  }
                successDialog.show(requireActivity().supportFragmentManager, "Confirmation Dialog")
            }
        }
        dialog.show(requireActivity().supportFragmentManager, "AssetTypeSelectionDialog")
    }

    private fun leaveBudget() {
        activeBudget.leave(FirebaseDatabaseProvider(requireActivity()))
        (requireActivity() as MainActivity).switchFragment(SharedBudgetEmptyFragment())
    }

    private fun setUpRecyclerViews() {
        invitationsRecycler.layoutManager = LinearLayoutManager(requireActivity())
        val adapterInvite = InvitationRecyclerAdapter(LocalDatabaseProvider(requireActivity()).getInvitations())
        invitationsRecycler.adapter = adapterInvite

        usersRecycler.layoutManager = LinearLayoutManager(requireActivity())
        val adapterUser = UserRecyclerAdapter(LocalDatabaseProvider(requireActivity()).getAllUsers().values.toList())
        usersRecycler.adapter = adapterUser
    }
}