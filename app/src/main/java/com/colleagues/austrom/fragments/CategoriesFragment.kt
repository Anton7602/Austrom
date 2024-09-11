package com.colleagues.austrom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter

class CategoriesFragment : Fragment(R.layout.fragment_categories) {
    private lateinit var categoriesRecyclerView : RecyclerView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        categoriesRecyclerView.layoutManager = LinearLayoutManager(activity)
        categoriesRecyclerView.adapter = CategoryRecyclerAdapter((requireActivity().application as AustromApplication).defaultCategories)
    }

    private fun bindViews(view: View) {
        categoriesRecyclerView = view.findViewById(R.id.cat_categoriesHolder_rcv)
    }

}