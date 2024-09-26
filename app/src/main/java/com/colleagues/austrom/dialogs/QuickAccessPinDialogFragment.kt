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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.R
import com.colleagues.austrom.fragments.SettingsFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class QuickAccessPinDialogFragment(private val oldPin : String?,
                                   private val parent: SettingsFragment) : DialogFragment() {
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private lateinit var oldPinText: TextInputEditText
    private lateinit var oldPinTextLayout: TextInputLayout
    private lateinit var newPinText: TextInputEditText
    private lateinit var newPinTextLayout: TextInputLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_quick_access_pin, null)
        bindViews(view)
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireActivity()).setView(view)
        val targetSelectionDialog = adb.create()
        if (targetSelectionDialog != null && targetSelectionDialog.window != null) {
            targetSelectionDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
        if (oldPin == null) {
            oldPinTextLayout.visibility = View.GONE
        }

        acceptButton.setOnClickListener {
            if (oldPin!=null && oldPinText.text.toString()==oldPin) {
                parent.setNewQuickAccessPin(newPinText.text.toString())
                dismiss()
            } else {
                Toast.makeText(requireActivity(), "Provided wrong old pin", Toast.LENGTH_LONG).show()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return targetSelectionDialog
    }

    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.qapdial_accept_btn)
        cancelButton = view.findViewById(R.id.qapdial_cancel_btn)
        oldPinText = view.findViewById(R.id.qapdial_oldPin_txt)
        oldPinTextLayout = view.findViewById(R.id.qapdial_oldPin_til)
        newPinText = view.findViewById(R.id.qapdial_newPin_txt)
        newPinTextLayout = view.findViewById(R.id.qapdial_newPin_til)
    }
}