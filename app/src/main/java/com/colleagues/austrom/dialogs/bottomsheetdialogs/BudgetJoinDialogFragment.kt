package com.colleagues.austrom.dialogs.bottomsheetdialogs

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
import com.colleagues.austrom.fragments.SharedBudgetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class BudgetJoinDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_budget_join, container, false) }
    fun setOnDialogResultListener(l: (()->Unit)) { returnResult = l }
    private var returnResult: ()->Unit = {}
    //region Binding
    private lateinit var joinNewBudget: Button
    private lateinit var inviteCodeTextView: TextInputEditText
    private lateinit var budgetNameTextView: TextInputEditText
    private lateinit var dialogHolder: CardView
    private fun bindViews(view: View) {
        joinNewBudget = view.findViewById(R.id.bjdial_submit_btn)
        inviteCodeTextView = view.findViewById(R.id.bjdial_InvitationCode_txt)
        budgetNameTextView = view.findViewById(R.id.bjdial_budgetName_txt)
        dialogHolder = view.findViewById(R.id.bjdial_holder_crd)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        inviteCodeTextView.requestFocus()
        joinNewBudget.setOnClickListener { joinNewBudget(); this.dismiss() }
    }
    private fun joinNewBudget() {
        val provider : IRemoteDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
        val budget = provider.getBudgetById(inviteCodeTextView.text.toString())
        val user = AustromApplication.appUser
        if (budget!=null && user!=null) {
            budget.users!!.add(user.userId)
            user.activeBudgetId = budget.budgetId
            provider.updateUser(user)
            provider.updateBudget(budget)
            (requireActivity() as MainActivity).switchFragment(SharedBudgetFragment(budget))
        }
    }
}