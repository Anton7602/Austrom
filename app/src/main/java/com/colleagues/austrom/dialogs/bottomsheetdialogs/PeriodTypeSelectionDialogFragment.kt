package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R
import com.colleagues.austrom.views.PeriodType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PeriodTypeSelectionDialogFragment() : BottomSheetDialogFragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate( R.layout.dialog_fragment_period_type_selection, container, false) }
    fun setOnDialogResultListener(l: (PeriodType)->Unit) { returnResult = l }
    private var returnResult: (PeriodType)->Unit = {}
    //region Binding
    private lateinit var dialogHolder: CardView
    private lateinit var week: Button
    private lateinit var month: Button
    private lateinit var year: Button
    private lateinit var custom: Button
    private fun bindViews(view: View) {
        dialogHolder = view.findViewById(R.id.datetypesel_holder_crv)
        week = view.findViewById(R.id.datetypesel_week_btn)
        month = view.findViewById(R.id.datetypesel_month_btn)
        year = view.findViewById(R.id.datetypesel_year_btn)
        custom = view.findViewById(R.id.datetypesel_custom_btn)
    }
    ///endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)

        week.setOnClickListener{returnResult(PeriodType.WEEK)}
        month.setOnClickListener{returnResult(PeriodType.MONTH)}
        year.setOnClickListener{returnResult(PeriodType.YEAR)}
        custom.setOnClickListener{returnResult(PeriodType.CUSTOM)}
    }

}