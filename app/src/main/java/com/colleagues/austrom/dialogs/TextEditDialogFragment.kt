package com.colleagues.austrom.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class TextEditDialogFragment(private val initialValue: String? = null, private val hint: String? = null, private val tip: String? = null)  : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_text_edit, container, false) }
    fun setOnDialogResultListener(l: ((String)->Unit)) { returnResult = l }
    private var returnResult: (String)->Unit = {}
    fun setOnTextChangedListener(l: ((String)->Unit)) { returnText = l }
    private var returnText: (String)->Unit = {}
    //region Binding
    private lateinit var textField: TextInputEditText
    private lateinit var textFieldTip: TextView
    private lateinit var textFieldLayout: TextInputLayout
    private lateinit var dialogHolder: CardView
    private lateinit var continueButton: ImageButton
    private fun bindViews(view: View) {
        textField = view.findViewById(R.id.txteditdial_textField_txt)
        textFieldTip = view.findViewById(R.id.txteditDial_textTip_txt)
        textFieldLayout = view.findViewById(R.id.txteditdial_textLayout_til)
        dialogHolder = view.findViewById(R.id.txteditdial_dialogHolder_crd)
        continueButton = view.findViewById(R.id.txteditdial_continue_btn)
    }
    // endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        if (!hint.isNullOrEmpty()) textFieldLayout.hint = hint
        textFieldTip.visibility = if (tip.isNullOrEmpty()) View.GONE else View.VISIBLE
        if (!tip.isNullOrEmpty()) textFieldTip.text = tip
        if (!initialValue.isNullOrEmpty()) textField.setText(initialValue)
        textField.addTextChangedListener { newText -> returnText(newText.toString()) }
        AustromApplication.showKeyboard(requireActivity(), textField)

        continueButton.setOnClickListener { dismiss() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        returnResult(textField.text.toString())
    }
}