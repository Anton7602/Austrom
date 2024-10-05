package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.interfaces.IDialogInitiator

class DeletionConfirmationDialogFragment(private var reciever: IDialogInitiator,
                                         private val message: String? = "The action you about to do is irreversible. Are you sure you want to proceed?",
                                         private val titleAccept: String? = "DELETE",
                                         private val titleDecline: String? = "CANCEL") : DialogFragment() {
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private lateinit var messageLabel: TextView

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

        acceptButton.setOnClickListener {
            reciever.receiveValue("true", "DialogResult")
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return targetSelectionDialog
    }

    private fun setUpValues() {
        acceptButton.text = titleAccept
        cancelButton.text = titleDecline
        messageLabel.text = message
    }

    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.wardial_delete_btn)
        cancelButton = view.findViewById(R.id.wardial_cancel_btn)
        messageLabel = view.findViewById(R.id.wardial_message_txt)
    }
}