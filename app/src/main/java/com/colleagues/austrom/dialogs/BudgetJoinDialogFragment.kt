package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class BudgetJoinDialogFragment : BottomSheetDialogFragment() {
    private lateinit var joinNewBudget: Button
    private lateinit var inviteCodeTextView: TextInputEditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        inviteCodeTextView.requestFocus()

        joinNewBudget.setOnClickListener {
            val provider : IDatabaseProvider = FirebaseDatabaseProvider()
            this.dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_budget_join, container, false)
    }

    private fun bindViews(view: View) {
        joinNewBudget = view.findViewById(R.id.bjdial_submit_btn)
        inviteCodeTextView = view.findViewById(R.id.bjdial_InvitationCode_txt)
    }
}