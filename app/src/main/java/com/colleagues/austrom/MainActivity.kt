package com.colleagues.austrom

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.transition.Visibility
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationBar: NavigationBarView
    private lateinit var balanceFragment: FragmentContainerView
    private lateinit var budgetFragment: FragmentContainerView
    private lateinit var opsFragment: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()


        bottomNavigationBar.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navbar_balance_mit -> {
                    budgetFragment.visibility = View.GONE
                    opsFragment.visibility = View.GONE
                    balanceFragment.visibility = View.VISIBLE
                    true
                }
                R.id.navbar_budget_mit -> {
                    balanceFragment.visibility = View.GONE
                    opsFragment.visibility = View.GONE
                    budgetFragment.visibility = View.VISIBLE
                    true
                }
                R.id.navbar_ops_mit -> {
                    budgetFragment.visibility = View.GONE
                    balanceFragment.visibility = View.GONE
                    opsFragment.visibility = View.VISIBLE
                    true
                }
                else -> {
                    budgetFragment.visibility = View.GONE
                    balanceFragment.visibility = View.GONE
                    opsFragment.visibility = View.GONE
                    false
                }
            }

        }
    }

    private fun bindViews() {
        bottomNavigationBar = findViewById(R.id.main_bottomNav_bnv)
        balanceFragment = findViewById(R.id.main_balance_frg)
        budgetFragment = findViewById(R.id.main_budget_frg)
        opsFragment = findViewById(R.id.main_ops_frg)
    }
}