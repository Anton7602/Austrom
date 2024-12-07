package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Category

class CategoriesFragment : Fragment(R.layout.fragment_categories), IDialogInitiator {
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
        val dbProvider: IRemoteDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
        dbProvider.updateUser(AustromApplication.appUser!!)
    }

    private fun setUpRecyclerView(recyclerView: RecyclerView, categories: MutableList<Category>) {
        recyclerView.adapter = CategoryRecyclerAdapter(categories, requireActivity() as AppCompatActivity, this, true)
        recyclerView.layoutManager = GridLayoutManager(activity, 5)
    }

    private fun setUpRecyclerViews() {
        setUpRecyclerView(expenseCategoriesRecyclerView, mergeDefaultAndCustomCategoriesLists(Category.defaultExpenseCategories, AustromApplication.activeExpenseCategories.values.toList()))
        setUpRecyclerView(transferCategoriesRecyclerView, mergeDefaultAndCustomCategoriesLists(Category.defaultTransferCategories, AustromApplication.activeTransferCategories.values.toList()))
        setUpRecyclerView(incomeCategoriesRecyclerView, mergeDefaultAndCustomCategoriesLists(Category.defaultIncomeCategories, AustromApplication.activeIncomeCategories.values.toList()))
    }

    private fun mergeDefaultAndCustomCategoriesLists(defaultCategories: List<Category>, customCategories: List<Category>) : MutableList<Category> {
        val mergedCategories = defaultCategories.toMutableList()
        for (category in customCategories) {
            if (!mergedCategories.contains(category)) {
                mergedCategories.add(category)
            }
        }
        return mergedCategories
    }

    override fun receiveValue(value: String, valueType: String) {
        setUpRecyclerViews()
    }

        private fun bindViews(view: View) {
        expenseCategoriesRecyclerView = view.findViewById(R.id.cat_expenseCategoriesHolder_rcv)
        transferCategoriesRecyclerView = view.findViewById(R.id.cat_transferCategoriesHolder_rcv)
        incomeCategoriesRecyclerView = view.findViewById(R.id.cat_incomeCategoriesHolder_rcv)
    }
}