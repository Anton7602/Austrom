package com.colleagues.austrom

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.QuickAccessPinDialogFragment
import com.colleagues.austrom.dialogs.SuggestQuickAccessDialogFragment
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.fragments.BudgetFragment
import com.colleagues.austrom.fragments.CategoriesFragment
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.fragments.SettingsFragment
import com.colleagues.austrom.fragments.SharedBudgetEmptyFragment
import com.colleagues.austrom.fragments.SharedBudgetFragment
import com.colleagues.austrom.models.Currency
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var toolbar : Toolbar
    private lateinit var filterButton: ImageButton
    private lateinit var navigationHeaderLayout : ConstraintLayout
    private lateinit var navigationView : NavigationView
    private lateinit var navigationUserNameTextView : TextView
    private lateinit var navigationLogOutButton: ImageButton
    private lateinit var bottomNavigationBar: NavigationBarView
    private lateinit var fragmentHolder: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        bindViews()
        setSupportActionBar(toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_drawerLayout_dly)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            //v.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = insets.bottom }
            //toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            toolbar.setPadding(0, insets.top, 0,0)
            navigationHeaderLayout.setPadding(0, insets.top, 0,0)
            bottomNavigationBar.setPadding(0,0,0,insets.bottom)

//          v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        val username = AustromApplication.appUser?.username
        if (username!=null) {
            navigationUserNameTextView.text = username.first().uppercaseChar()+username.substring(1)
        }
        val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
        AustromApplication.activeCurrencies =
            Currency.switchRatesToNewBaseCurrency(dbProvider.getCurrencies(), AustromApplication.appUser?.baseCurrencyCode)

        if (intent.getBooleanExtra("newUser",false)) {
            SuggestQuickAccessDialogFragment().show(supportFragmentManager, "Suggest Quick Access Code Dialog")
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        filterButton.setOnClickListener {

        }

        navigationView.setNavigationItemSelectedListener { item ->
            var status = true
            when(item.itemId) {
                R.id.nav_sharedBudget_mit -> {
                    val provider: IDatabaseProvider = FirebaseDatabaseProvider(this)
                    val activeBudgetId = AustromApplication.appUser?.activeBudgetId
                    if (activeBudgetId!=null) {
                        val activeBudget = provider.getBudgetById(activeBudgetId)
                        if (activeBudget!=null) {
                            switchFragment(SharedBudgetFragment(activeBudget))
                        }
                        else {
                            switchFragment(SharedBudgetEmptyFragment())
                        }
                    } else {
                        switchFragment(SharedBudgetEmptyFragment())
                    }
                }
                R.id.nav_categories_mit ->  switchFragment(CategoriesFragment())
                R.id.nav_settings_mit ->  switchFragment(SettingsFragment())
                else -> status = false
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            status
        }

        bottomNavigationBar.setOnItemSelectedListener { item ->
            var status = true
            when(item.itemId) {
                R.id.navbar_balance_mit -> switchFragment(BalanceFragment())
                R.id.navbar_budget_mit -> switchFragment(BudgetFragment())
                R.id.navbar_ops_mit -> switchFragment(OpsFragment())
                else -> status = false
            }
            status
        }

        navigationLogOutButton.setOnClickListener {
            (application as AustromApplication).forgetRememberedUser()
            (application as AustromApplication).forgetRememberedPin()
            this.finish()
        }

        onBackPressedDispatcher.addCallback(this) {
        }
    }

    fun switchFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragmentHolder_frg, fragment)
        transaction.commit()
    }

    private fun bindViews() {
        drawerLayout = findViewById(R.id.main_drawerLayout_dly)
        toolbar = findViewById(R.id.main_toolbar_tbr)
        filterButton = findViewById(R.id.main_filter_btn)
        navigationView = findViewById(R.id.main_navigationView_nvw)
        bottomNavigationBar = findViewById(R.id.main_bottomNav_bnv)
        fragmentHolder = findViewById(R.id.main_fragmentHolder_frg)
        val navigationHeader = navigationView.getHeaderView(0)
        navigationUserNameTextView = navigationHeader.findViewById(R.id.nav_username_txt)
        navigationLogOutButton = navigationHeader.findViewById(R.id.nav_logout_btn)
        navigationHeaderLayout = navigationHeader.findViewById(R.id.nav_navHeader_cly)
    }
}