package com.colleagues.austrom.dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.colleagues.austrom.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TransactionCreationDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_transaction_creation, container, false)
    }
}