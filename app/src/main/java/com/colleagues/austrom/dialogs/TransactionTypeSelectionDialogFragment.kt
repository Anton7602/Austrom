package com.colleagues.austrom.dialogs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AssetCreationActivity
import com.colleagues.austrom.LiabilityCreationActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetTypeRecyclerAdapter
import com.colleagues.austrom.adapters.TransactionTypeRecyclerAdapter
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TransactionTypeSelectionDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_transaction_type_selection, container, false) }
    fun setOnDialogResultListener(l: (TransactionType)->Unit) { returnResult = l }
    private var returnResult: (TransactionType)->Unit = {}
    //region Binding
    private lateinit var transactionTypeHolder: RecyclerView
    private lateinit var dialogHolder: CardView
    private fun bindViews(view: View) {
        transactionTypeHolder = view.findViewById(R.id.trtypeseldial_assetHolder_rcv)
        dialogHolder = view.findViewById(R.id.trtypeseldial_holder_crv)
    }
    ///endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        setUpRecyclerViews()
    }


    private fun setUpRecyclerViews() {
        transactionTypeHolder.layoutManager = LinearLayoutManager(activity)
        val adapterTransactionType = TransactionTypeRecyclerAdapter(TransactionType.entries, requireActivity() as AppCompatActivity)
        adapterTransactionType.setOnItemClickListener { transactionType -> returnResult(transactionType)}
        transactionTypeHolder.adapter = adapterTransactionType
    }
}