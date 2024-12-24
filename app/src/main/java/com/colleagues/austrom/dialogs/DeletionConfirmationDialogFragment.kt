package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.R

class DeletionConfirmationDialogFragment(private var message: String? = null, private var titleAccept: String? = null,  private var titleDecline: String? = null) : DialogFragment() {
    fun setOnDialogResultListener(l: ((Boolean)->Unit)) { returnResult = l }
    private var returnResult: (Boolean)->Unit = {}
    //region Binding
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private lateinit var messageLabel: TextView
    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.wardial_delete_btn)
        cancelButton = view.findViewById(R.id.wardial_cancel_btn)
        messageLabel = view.findViewById(R.id.wardial_message_txt)
    }
    // endregion

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_deletion_confirmation, null)
        bindViews(view)
        setUpValues()
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireActivity()).setView(view)
        val targetSelectionDialog = adb.create()
        if (targetSelectionDialog != null && targetSelectionDialog.window != null) {
            targetSelectionDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        acceptButton.setOnClickListener { returnResult(true); dismiss() }
        cancelButton.setOnClickListener { dismiss() }
        return targetSelectionDialog
    }

    private fun setUpValues() {
        acceptButton.text = if (titleAccept==null) requireActivity().getString(R.string.delete) else titleAccept
        cancelButton.text = if (titleDecline==null) requireActivity().getString(R.string.cancel) else titleDecline
        messageLabel.text = if (message==null) requireActivity().getString(R.string.the_action_you_about_to_do_is_irreversible_are_you_sure_you_want_to_proceed) else message
    }
}