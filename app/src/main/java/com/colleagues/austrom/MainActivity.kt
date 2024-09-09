package com.colleagues.austrom

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var toolbar : Toolbar
    private lateinit var navigationView : NavigationView
    private lateinit var bottomNavigationBar: NavigationBarView
    private lateinit var balanceFragment: FragmentContainerView
    private lateinit var budgetFragment: FragmentContainerView
    private lateinit var opsFragment: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_drawerLayout_dly)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()
        setSupportActionBar(toolbar)

        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


//        val username = (this.application as AustromApplication).appUser?.username
//        Toast.makeText(this, "Current username is $username", Toast.LENGTH_LONG).show()


//        bottomNavigationBar.setOnItemSelectedListener { item ->
//            when(item.itemId) {
//                R.id.navbar_balance_mit -> {
//                    budgetFragment.visibility = View.GONE
//                    opsFragment.visibility = View.GONE
//                    balanceFragment.visibility = View.VISIBLE
//                    true
//                }
//                R.id.navbar_budget_mit -> {
//                    balanceFragment.visibility = View.GONE
//                    opsFragment.visibility = View.GONE
//                    budgetFragment.visibility = View.VISIBLE
//                    true
//                }
//                R.id.navbar_ops_mit -> {
//                    budgetFragment.visibility = View.GONE
//                    balanceFragment.visibility = View.GONE
//                    opsFragment.visibility = View.VISIBLE
//                    true
//                }
//                else -> {
//                    budgetFragment.visibility = View.GONE
//                    balanceFragment.visibility = View.GONE
//                    opsFragment.visibility = View.GONE
//                    false
//                }
//            }
//        }
    }

    private fun switchFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragmentHolder_frg, fragment)
        transaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }

    private fun bindViews() {
        drawerLayout = findViewById(R.id.main_drawerLayout_dly)
        toolbar = findViewById(R.id.main_toolbar_tbr)
        navigationView = findViewById(R.id.main_navigationView_nvw)
        bottomNavigationBar = findViewById(R.id.main_bottomNav_bnv)
        balanceFragment = findViewById(R.id.main_fragmentHolder_frg)
        //budgetFragment = findViewById(R.id.main_budget_frg)
        //opsFragment = findViewById(R.id.main_ops_frg)
    }
}