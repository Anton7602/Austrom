package com.colleagues.austrom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.Category

class CategoriesFragment : Fragment(R.layout.fragment_categories) {
    private lateinit var expenseCategoriesRecyclerView : RecyclerView
    private lateinit var transferCategoriesRecyclerView : RecyclerView
    private lateinit var incomeCategoriesRecyclerView : RecyclerView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerViews()


    }

    override fun onPause() {
        super.onPause()
        val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
        dbProvider.updateUser(AustromApplication.appUser!!)
    }

    private fun setUpRecyclerViews() {
        expenseCategoriesRecyclerView.layoutManager = GridLayoutManager(activity, 4)
        expenseCategoriesRecyclerView.adapter = CategoryRecyclerAdapter(Category.defaultExpenseCategories)
        transferCategoriesRecyclerView.adapter = CategoryRecyclerAdapter(Category.defaultTransferCategories)
        transferCategoriesRecyclerView.layoutManager = GridLayoutManager(activity, 4)
        incomeCategoriesRecyclerView.adapter = CategoryRecyclerAdapter(Category.defaultIncomeCategories)
        incomeCategoriesRecyclerView.layoutManager = GridLayoutManager(activity, 4)
    }

    private fun bindViews(view: View) {
        expenseCategoriesRecyclerView = view.findViewById(R.id.cat_expenseCategoriesHolder_rcv)
        transferCategoriesRecyclerView = view.findViewById(R.id.cat_transferCategoriesHolder_rcv)
        incomeCategoriesRecyclerView = view.findViewById(R.id.cat_incomeCategoriesHolder_rcv)
    }

}