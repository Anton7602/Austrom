package com.colleagues.austrom.dialogs.sidesheetdialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.colleagues.austrom.R
import com.google.android.material.sidesheet.SideSheetDialog

class AnalyticsSelectionDialogFragment(context: Context) : SideSheetDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_fragment_analytics_selection)
        //setSheetEdge(Gravity.START)


        //window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        //window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}