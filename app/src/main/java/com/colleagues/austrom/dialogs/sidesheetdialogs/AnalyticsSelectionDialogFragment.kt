package com.colleagues.austrom.dialogs.sidesheetdialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.fragments.AnalyticsNetWorthHistoryFragment
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.fragments.AnalyticsSplitByCategoryFragment
import com.google.android.material.sidesheet.SideSheetDialog

class AnalyticsSelectionDialogFragment(context: Context) : SideSheetDialog(context) {
    fun setOnAnalyticsSelectedListener(l: (Fragment)->Unit) { returnSelectedAnalyticsScreen = l }
    private var returnSelectedAnalyticsScreen: (Fragment)->Unit = {}
    //region Binding
    private lateinit var expenseByCategorySplitButton: CardView
    private lateinit var netWorthHistoryButton: CardView
    private fun bindViews(view: View) {
        expenseByCategorySplitButton = view.findViewById(R.id.analseldial_expenseByCategory_crv)
        netWorthHistoryButton = view.findViewById(R.id.analseldial_netWorthHistory_crv)
    }
    ///endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_fragment_analytics_selection)
        val rootView = window?.decorView?.findViewById<View>(android.R.id.content)
        rootView?.let { bindViews(it) }

        expenseByCategorySplitButton.setOnSafeClickListener {
            returnSelectedAnalyticsScreen(AnalyticsSplitByCategoryFragment())
            this.dismiss()
        }

        netWorthHistoryButton.setOnSafeClickListener {
            returnSelectedAnalyticsScreen(AnalyticsNetWorthHistoryFragment())
            this.dismiss()
        }

        //setSheetEdge(Gravity.START)


        //window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        //window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }



}