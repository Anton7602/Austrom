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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OpsFragment : Fragment(R.layout.fragment_ops) {
    private lateinit var transactionHolder: RecyclerView
    private lateinit var addNewAssetButton: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        val provider : IDatabaseProvider = FirebaseDatabaseProvider()
        val transactionList = (requireActivity().application as AustromApplication).appUser?.let { provider.getTransactionsOfUser(it, requireActivity()) }
        if (transactionList!=null) {
            transactionHolder.layoutManager = LinearLayoutManager(activity)
            transactionHolder.adapter = TransactionRecyclerAdapter(transactionList.toList())
        }

        addNewAssetButton.setOnClickListener {
            TransactionCreationDialogFragment().show(requireActivity().supportFragmentManager, "Asset Creation Dialog")
        }
    }

    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.ops_transactionHolder_rcv)
        addNewAssetButton = view.findViewById(R.id.ops_addNew_fab)
    }
}