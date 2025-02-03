package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.SelectorRecyclerAdapter
import com.colleagues.austrom.extensions.startWithUppercase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class ImportMappingDialogFragment(private val fileColumnNames: List<String>, private val appValues: List<String>,  private val keyMap: String,
                                  private val isFromAppByDefault: Boolean = false, private val selectedValueDefault: String? = null) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_import_mapping, container, false) }
    fun setOnDialogResultListener(l: ((isFromFile: Boolean, value: String, keyMap: String)->Unit)) { returnResult = l }
    private var returnResult: (Boolean, String, String) -> Unit = { _, _, _ -> }
    //region Binding
    private lateinit var fromFileValuesHolder: RecyclerView
    private lateinit var fromAppValuesHolder: RecyclerView
    private lateinit var dialogHolder: CardView
    private lateinit var fromFileSwitchButton: Button
    private lateinit var fromAppSwitchButton: Button
    private lateinit var manualInputLayout: LinearLayout
    private lateinit var manualInputTextInputLayout: TextInputLayout
    private lateinit var manualInputTextView: TextInputEditText
    private lateinit var acceptButton: ImageButton
    private fun bindViews(view: View) {
        fromFileValuesHolder = view.findViewById(R.id.impmapdial_fileFields_rcv)
        fromAppValuesHolder = view.findViewById(R.id.impmapdial_appValues_rcv)
        dialogHolder = view.findViewById(R.id.impmapdial_holder_crv)
        fromFileSwitchButton = view.findViewById(R.id.impmapdial_fromFile_btn)
        fromAppSwitchButton = view.findViewById(R.id.impmapdial_fromApplication_btn)
        manualInputLayout = view.findViewById(R.id.impmapdial_manualInput_lly)
        manualInputTextView = view.findViewById(R.id.impmapdial_manualInput_txt)
        manualInputTextInputLayout = view.findViewById(R.id.impmapdial_manualInput_til)
        acceptButton = view.findViewById(R.id.impmapdial_accept_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerViews()
        manualInputTextInputLayout.hint = keyMap.startWithUppercase()
        manualInputTextView.inputType = when(keyMap) {
            "amount" -> InputType.TYPE_CLASS_NUMBER
            "date" -> EditorInfo.TYPE_CLASS_DATETIME
            else -> manualInputTextView.inputType
        }
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        fromFileSwitchButton.setOnClickListener { switchFromAppVisibility(true) }
        fromAppSwitchButton.setOnClickListener { switchFromAppVisibility(false) }
        acceptButton.setOnClickListener { returnResult(false, manualInputTextView.text.toString(), keyMap); dismiss() }

        if (isFromAppByDefault) switchFromAppVisibility(false)
    }

    private fun switchFromAppVisibility(isFromFileSelected: Boolean) {
        fromFileValuesHolder.visibility = if (isFromFileSelected) View.VISIBLE else View.GONE
        fromAppValuesHolder.visibility = if (!isFromFileSelected && appValues.isNotEmpty()) View.VISIBLE else View.GONE
        manualInputLayout.visibility = if (!isFromFileSelected && appValues.isEmpty()) View.VISIBLE else View.GONE
        fromFileSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(if (isFromFileSelected) R.color.blue else R.color.grey))
        fromAppSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(if (isFromFileSelected) R.color.grey else R.color.blue))
    }

    private fun setUpRecyclerViews() {
        fromFileValuesHolder.layoutManager = LinearLayoutManager(activity)
        val adapterFromFile = SelectorRecyclerAdapter(fileColumnNames, selectedValueDefault)
        adapterFromFile.setOnItemClickListener { selectedValue -> returnResult(fromFileValuesHolder.visibility==View.VISIBLE, selectedValue, keyMap); dismiss() }
        fromFileValuesHolder.adapter = adapterFromFile

        fromAppValuesHolder.layoutManager = LinearLayoutManager(activity)
        val adapterFromApp = SelectorRecyclerAdapter(appValues, selectedValueDefault)
        adapterFromApp.setOnItemClickListener { selectedValue -> returnResult(fromFileValuesHolder.visibility==View.VISIBLE ,selectedValue, keyMap); dismiss()  }
        fromAppValuesHolder.adapter = adapterFromApp
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerViews()
    }
}