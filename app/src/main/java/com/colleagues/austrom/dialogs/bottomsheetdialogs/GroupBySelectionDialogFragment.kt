package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.TransactionGroupByType
import com.colleagues.austrom.views.PeriodType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupBySelectionDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate( R.layout.dialog_fragment_group_by_selection, container, false) }
    fun setOnDialogResultListener(l: (TransactionGroupByType)->Unit) { returnResult = l }
    private var returnResult: (TransactionGroupByType)->Unit = {}
    //region Binding
    private lateinit var dialogHolder: CardView
    private lateinit var byDate: Button
    private lateinit var byCategory: Button
    private lateinit var byName: Button
    private fun bindViews(view: View) {
        dialogHolder = view.findViewById(R.id.groupbysel_holder_crv)
        byDate = view.findViewById(R.id.groupbysel_byDate_btn)
        byCategory = view.findViewById(R.id.groupbysel_byCategory_btn)
        byName = view.findViewById(R.id.groupbysel_byName_btn)
    }
    ///endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        byDate.setOnClickListener{returnResult(TransactionGroupByType.BY_DATE)}
        byCategory.setOnClickListener{returnResult(TransactionGroupByType.BY_CATEGORY)}
        byName.setOnClickListener{returnResult(TransactionGroupByType.BY_NAME)}
    }
}