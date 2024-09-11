package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import java.util.Calendar
import java.util.GregorianCalendar


class TransactionCreationDialogFragment : BottomSheetDialogFragment() {
    private lateinit var categoryChips: ChipGroup
    private lateinit var dateChips: ChipGroup


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_transaction_creation, container, false)
    }

//    private fun setUpCategoriesInChips() {
//        val categories = (requireActivity().application as AustromApplication).defaultCategories
//        for (category in categories) {
//            val chip = Chip(requireActivity())
//            chip.text = category.name
//            chip.chipIcon = ContextCompat.getDrawable(requireActivity(), category.imgReference ?: R.drawable.ic_placeholder_icon)
//            chip.setEnsureMinTouchTargetSize(false)
//            chip.setChipDrawable(
//                ChipDrawable.createFromAttributes(
//                    requireActivity(),
//                    null,
//                    0,
//                    R.style.Widget_MaterialComponents_Chip_Choice
//                )
//            )
//            if (category == categories[0]) {
//                chip.isChecked = true
//            }
//            categoryChips.addView(chip)
//        }
//    }
//
//    private fun setUpDateChips() {
//        val calendar: Calendar = Calendar.getInstance()
//        val currentYear: Int = calendar.get(Calendar.YEAR)
//        var currentMonth: Int = calendar.get(Calendar.MONTH)
//        var currentDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
//        var newCalendar: Calendar = GregorianCalendar(currentYear, currentMonth, 1)
//        for (i in 0..9) {
//            val chip = Chip(requireActivity())
//            chip.setText(dateHelper.dateParseToString(currentYear, currentMonth, currentDay))
//            chip.setEnsureMinTouchTargetSize(false)
//            chip.setChipDrawable(
//                ChipDrawable.createFromAttributes(
//                    this,
//                    null,
//                    0,
//                    R.style.Widget_MaterialComponents_Chip_Choice
//                )
//            )
//            dateChips.addView(chip)
//            if (currentDay > 1) {
//                currentDay -= 1
//            } else {
//                currentMonth -= 1
//                newCalendar = GregorianCalendar(currentYear, currentMonth, 1)
//                currentDay = newCalendar.getActualMaximum(calendar.DAY_OF_MONTH)
//            }
//            if (i == 0) {
//                chip.isChecked = true
//            }
//        }
//    }
}