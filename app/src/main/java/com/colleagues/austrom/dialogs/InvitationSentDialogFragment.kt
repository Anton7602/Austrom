package com.colleagues.austrom.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InvitationSentDialogFragment(private val invitationCode: String) : DialogFragment() {
    fun setOnDialogResultListener(l: ((Boolean)->Unit)) { returnResult = l }
    private var returnResult: (Boolean)->Unit = {}
    //region Binding
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button
    private lateinit var invitationCodeTextView: TextView
    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.invitesentdial_accept_btn)
        declineButton = view.findViewById(R.id.invitesentdial_decline_btn)
        invitationCodeTextView = view.findViewById(R.id.invitesentdial_invitationCode_txt)
    }
    // endregion

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val customView: View = requireActivity().layoutInflater.inflate(R.layout.dialog_fragment_invitation_sent, null)
        bindViews(customView)

        invitationCodeTextView.text = invitationCode
        acceptButton.setOnClickListener { returnResult(true); dismiss() }
        declineButton.setOnClickListener { returnResult(false); dismiss() }

        return MaterialAlertDialogBuilder(requireContext()).setView(customView).create()
    }
}