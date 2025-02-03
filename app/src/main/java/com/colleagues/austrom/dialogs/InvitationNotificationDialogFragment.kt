package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryArrayAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Category
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InvitationNotificationDialogFragment : DialogFragment() {
    fun setOnDialogResultListener(l: ((Boolean)->Unit)) { returnResult = l }
    private var returnResult: (Boolean)->Unit = {}
    //region Binding
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button
    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.invitedial_accept_btn)
        declineButton = view.findViewById(R.id.invitedial_decline_btn)
    }
    // endregion

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val customView: View = requireActivity().layoutInflater.inflate(R.layout.dialog_fragment_invitation_notification, null)
        bindViews(customView)

        acceptButton.setOnClickListener { returnResult(true); dismiss() }
        declineButton.setOnClickListener { returnResult(false); dismiss() }

        return MaterialAlertDialogBuilder(requireContext()).setView(customView).create()
    }
}