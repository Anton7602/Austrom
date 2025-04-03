package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.cardview.widget.CardView.TEXT_ALIGNMENT_CENTER
import androidx.core.content.ContextCompat
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.models.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class NamePickerDialogFragment(private val namesList: List<String>) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_name_picker, container, false) }
    fun setOnSelectionChangedListener(l: ((selectionState: Boolean, name: String)->Unit)) { notifySelectionChanged = l }
    private var notifySelectionChanged: (Boolean, String) -> Unit = { _, _ -> }
    //region Binding
    private lateinit var nameHolder: ChipGroup
    private lateinit var dialogHolder: CardView
    private lateinit var nameTextEdit: TextInputEditText
    private lateinit var nameTextInputLayout: TextInputLayout
    private lateinit var addNewChipButton: ImageButton
    private fun bindViews(view: View) {
        nameHolder = view.findViewById(R.id.catpickdial_categoryHolder_crv)
        dialogHolder = view.findViewById(R.id.namepickdial_holder_crv)
        nameTextEdit = view.findViewById(R.id.namepickdial_nameField_txt)
        nameTextInputLayout = view.findViewById(R.id.namepickdial_nameField_til)
        addNewChipButton = view.findViewById(R.id.namepickdial_addChip_btn)
    }
    ///endregion
    private var nameTextListener: TextWatcher? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        generateChipsForExistingNames(namesList)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        addNewChipButton.setOnClickListener { addNewFilterName(nameTextEdit.text.toString()) }

    }

    private fun generateChipsForExistingNames(names: List<String>) {
        names.forEach { name -> generateChip(name) }
    }

    private fun isCheckNameAllowed(name: String): Boolean {
        if (nameTextListener==null)
            nameTextListener = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {isCheckNameAllowed(nameTextEdit.text.toString())}
                override fun afterTextChanged(s: Editable?) {}
            }
        if (name.isEmpty()) {
            nameTextEdit.addTextChangedListener(nameTextListener)
            return false
        }
        nameTextEdit.removeTextChangedListener(nameTextListener)
        return true
    }


    private fun addNewFilterName(chipName: String) {
        if (!isCheckNameAllowed(chipName)) {
            nameTextInputLayout.error = "Provided Name Is Not Allowed Here"
            return
        }
        nameTextEdit.setText("")
        generateChip(chipName)
        notifySelectionChanged(true, chipName)
    }


    private fun generateChip(chipName: String) {
        nameHolder.addView(Chip(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(context.dpToPx(8)).build()
            chipBackgroundColor = (context.getColorStateList(R.color.chip_filter_background_color))
            setChipIconTintResource(R.color.chip_filter_text_color)
            setTextColor(context.getColorStateList(R.color.chip_filter_text_color))
            setPadding(0,context.dpToPx(0).toInt(),0,context.dpToPx(0).toInt())
            textAlignment = TEXT_ALIGNMENT_CENTER
            setEnsureMinTouchTargetSize(false)
            minimumHeight = 0
            text = chipName
            isChipIconVisible = true
            isCheckable = true
            isChecked = true
            tag = chipName
            setOnClickListener { _ -> notifySelectionChanged(this.isChecked, chipName) }
        })
    }
}