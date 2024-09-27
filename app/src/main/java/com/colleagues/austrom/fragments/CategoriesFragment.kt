package com.colleagues.austrom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.models.Category

class CategoriesFragment : Fragment(R.layout.fragment_categories) {
    private lateinit var categoriesRecyclerView : RecyclerView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        categoriesRecyclerView.layoutManager = LinearLayoutManager(activity)
        categoriesRecyclerView.adapter = CategoryRecyclerAdapter(Category.defaultExpenseCategories)
    }

    private fun bindViews(view: View) {
        categoriesRecyclerView = view.findViewById(R.id.cat_categoriesHolder_rcv)
    }

}