package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.adapters.TransactionRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.AssetCreationDialogFragment
import com.colleagues.austrom.dialogs.TransactionCreationDialogFragment
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OpsFragment : Fragment(R.layout.fragment_ops) {
    private lateinit var transactionHolder: RecyclerView
    private lateinit var addNewAssetButton: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        updateTransactionsList()

        addNewAssetButton.setOnClickListener {
            TransactionCreationDialogFragment(this, TransactionType.EXPENSE).show(requireActivity().supportFragmentManager, "Asset Creation Dialog")
        }
    }

    fun updateTransactionsList() {
        val provider : IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
        val transactionList = AustromApplication.appUser?.let { provider.getTransactionsOfUser(it) }
        if (transactionList!=null) {
            setUpRecyclerView(transactionList)
        }
    }

    private fun setUpRecyclerView(transactionList: MutableList<Transaction>) {
        transactionHolder.layoutManager = LinearLayoutManager(activity)
        transactionHolder.adapter = TransactionRecyclerAdapter(transactionList.toList())
    }

    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.ops_transactionHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.ops_addNew_fab)
    }
}