package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.cardview.widget.CardView.TEXT_ALIGNMENT_CENTER
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.models.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel

class CategoryPickerDialogFragment(private val categoryList: Map<Category, Boolean>) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_category_picker, container, false) }
    fun setOnSelectionChangedListener(l: ((selectionState: Boolean, category: Category)->Unit)) { notifySelectionChanged = l }
    private var notifySelectionChanged: (Boolean, Category) -> Unit = { _, _ -> }
    //region Binding
    private lateinit var categoryHolder: ChipGroup
    private lateinit var dialogHolder: CardView
    private lateinit var inverseButton: ImageButton
    private fun bindViews(view: View) {
        categoryHolder = view.findViewById(R.id.catpickdial_categoryHolder_crv)
        dialogHolder = view.findViewById(R.id.catpickdial_holder_crv)
        inverseButton = view.findViewById(R.id.catpickdial_inverseSelection_btn)
    }
    ///endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        inverseButton.setOnSafeClickListener { categoryHolder.children.forEach { child -> if (child is Chip) { child.isChecked = !child.isChecked; notifySelectionChanged(child.isChecked, child.tag as Category) }} }
        setUpCategoriesInChips(categoryList)
    }

    private fun setUpCategoriesInChips(categories: Map<Category, Boolean>) {
        for (entry in categories) {
            val category = entry.key
            categoryHolder.addView(Chip(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(context.dpToPx(8)).build()
                chipIcon = ContextCompat.getDrawable(context, category.imgReference.resourceId)
                chipBackgroundColor = (context.getColorStateList(R.color.chip_filter_background_color))
                setChipIconTintResource(R.color.chip_filter_text_color)
                setTextColor(context.getColorStateList(R.color.chip_filter_text_color))
                setPadding(0,context.dpToPx(0).toInt(),0,context.dpToPx(0).toInt())
                textAlignment = TEXT_ALIGNMENT_CENTER
                setEnsureMinTouchTargetSize(false)
                minimumHeight = 0
                text = category.name
                isChipIconVisible = true
                isCheckable = true
                isChecked = entry.value
                tag = category
                setOnClickListener { _ -> notifySelectionChanged(this.isChecked, category)
//                    if (this.isChecked) {
//                        transactionFilter.categories.add(chip.tag.toString())
//                        chipHeader.isChecked = true
//                        secondaryChipHeader?.isChecked = true
//                    } else {
//                        transactionFilter.categories.remove(chip.tag.toString())
//                        chipHeader.isChecked = false
//                        secondaryChipHeader?.isChecked = false
//                        chipGroup.children.forEach { view -> if (view is Chip && view.isChecked) chipHeader.isChecked = true; }
//                    }
//                    returnFilter(transactionFilter)
                }
            })
            //transactionFilter.categories.add(category.categoryId)
        }
    }
}