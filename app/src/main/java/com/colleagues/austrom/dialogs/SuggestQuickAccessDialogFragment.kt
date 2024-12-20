package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SuggestQuickAccessDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_suggest_quick_access, container, false) }
    fun setOnDialogResultListener(l: (()->Unit)) { returnResult = l }
    private var returnResult: ()->Unit = {}
    //region Binding
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private lateinit var dialogHolder: CardView
    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.qactip_accept_btn)
        cancelButton = view.findViewById(R.id.qactip_decline_btn)
        dialogHolder = view.findViewById(R.id.qactip_holder_crv)
    }
    // endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        acceptButton.setOnClickListener { QuickAccessPinDialogFragment().show(requireActivity().supportFragmentManager, "Pin Changing Dialog"); this.dismiss() }
        cancelButton.setOnClickListener { this.dismiss() }
    }
}