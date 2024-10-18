package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class TransactionCreationDialogFragment2(private val transactionType: TransactionType,
                                         private val parentDialog: IDialogInitiator? = null): BottomSheetDialogFragment() {
    private lateinit var constructorHolder: CardView
    private lateinit var stage1Layout: ConstraintLayout
    private lateinit var stage2Layout: ConstraintLayout
    private lateinit var foreignTextView: TextInputEditText
    private lateinit var forwardButton: ImageButton
    private var currentStageId = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_transaction_creation2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        constructorHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        setStage(0)


        forwardButton.setOnClickListener {
            setStage(currentStageId++)
        }


    }

    private fun setStage(stageId: Int) {
        if (stageId==0) AustromApplication.showKeyboard(requireActivity(), foreignTextView)
        if (stageId>0) AustromApplication.hideKeyboard(requireActivity(), foreignTextView)
        stage1Layout.visibility = if (stageId==0) View.VISIBLE else View.GONE
        stage2Layout.visibility = if (stageId==1) View.VISIBLE else View.GONE
    }

    private fun bindViews(view: View) {
        constructorHolder = view.findViewById(R.id.ctdial2_holder_crv)
        foreignTextView = view.findViewById(R.id.ctdial2_foreignName_txt)
        forwardButton = view.findViewById(R.id.ctdial2_forward_btn)
        stage1Layout = view.findViewById(R.id.ctdial2_stage1Layout_cly)
        stage2Layout = view.findViewById(R.id.ctdial2_stage2Layout_cly)
    }
}