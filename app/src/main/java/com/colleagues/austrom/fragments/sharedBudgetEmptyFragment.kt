package com.colleagues.austrom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.colleagues.austrom.R

class sharedBudgetEmptyFragment() : Fragment(R.layout.fragment_shared_budget_empty) {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shared_budget_empty, container, false)
    }

}