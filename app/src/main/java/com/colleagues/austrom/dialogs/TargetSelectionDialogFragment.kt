package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.R

class TargetSelectionDialogFragment(private var parentDialog : TransactionCreationDialogFragment? = null) : DialogFragment() {
    private lateinit var searchView: EditText
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_target_selection, null)
        bindViews(view)
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireActivity()).setView(view)
        val targetSelectionDialog = adb.create()
        if (targetSelectionDialog != null && targetSelectionDialog.window != null) {
            targetSelectionDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }

        acceptButton.setOnClickListener {
            if(parentDialog!=null) {
                parentDialog!!.receiveTargetSelection(searchView.text.toString())
            }
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return targetSelectionDialog
    }

    private fun bindViews(view: View) {
        searchView = view.findViewById(R.id.tsdial_searchField_txt)
        acceptButton = view.findViewById(R.id.tsdial_acceptButton_btn)
        cancelButton = view.findViewById(R.id.tsdial_cancelButton_btn)
    }

}