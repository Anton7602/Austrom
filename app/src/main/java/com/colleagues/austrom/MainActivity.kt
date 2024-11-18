package com.colleagues.austrom

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
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
import com.colleagues.austrom.dialogs.AssetFilterDialogFragment
import com.colleagues.austrom.dialogs.SuggestQuickAccessDialogFragment
import com.colleagues.austrom.dialogs.TransactionFilterDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.fragments.BudgetFragment
import com.colleagues.austrom.fragments.CategoriesFragment
import com.colleagues.austrom.fragments.ExportFragment
import com.colleagues.austrom.fragments.ImportFragment
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

    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.attachBaseContext(newBase)
        } else  {
            super.attachBaseContext(AustromApplication.updateBaseContextLocale(newBase))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        bindViews()
        setSupportActionBar(toolbar)
        adjustInsets()
        downloadCashedValues()
        navigationUserNameTextView.text = AustromApplication.appUser?.username!!.startWithUppercase()
        suggestSettingUpQuickAccessCode()
        setUpNavigationDrawer()

        filterButton.setOnClickListener {
            when (fragmentHolder.getFragment<Fragment>()) {
                is BalanceFragment -> AssetFilterDialogFragment(fragmentHolder.getFragment()).show(supportFragmentManager, "Suggest Quick Access Code Dialog")
                is OpsFragment -> TransactionFilterDialogFragment(fragmentHolder.getFragment()).show(supportFragmentManager, "Suggest Quick Access Code Dialog")
            }

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
                R.id.nav_export_mit ->  switchFragment(ExportFragment())
                R.id.nav_import_mit ->  switchFragment(ImportFragment())
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
            AustromApplication.appUser = null
            AustromApplication.activeAssets = mutableMapOf()
            AustromApplication.knownUsers = mutableMapOf()
            this.finish()
        }

        onBackPressedDispatcher.addCallback(this) {
        }
    }

    fun switchFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragmentHolder_frg, fragment)
        when (fragment) {
            is BalanceFragment -> filterButton.visibility = View.VISIBLE
            is OpsFragment -> filterButton.visibility = View.VISIBLE
            else -> filterButton.visibility = View.GONE
        }
        transaction.commit()
    }

    private fun suggestSettingUpQuickAccessCode() {
        if (intent.getBooleanExtra("newUser",false) && (application as AustromApplication).getRememberedPin()==null) {
            SuggestQuickAccessDialogFragment().show(supportFragmentManager, "Suggest Quick Access Code Dialog")
        }
    }

    private fun downloadCashedValues() {
        val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
        if (AustromApplication.activeCurrencies.isEmpty()) {
            AustromApplication.activeCurrencies = Currency.switchRatesToNewBaseCurrency(
                Currency.localizeCurrencyNames(dbProvider.getCurrencies(), this), AustromApplication.appUser?.baseCurrencyCode)
        }
        AustromApplication.activeCurrencies = Currency.localizeCurrencyNames(AustromApplication.activeCurrencies, this)
        if (AustromApplication.appUser?.activeBudgetId!=null) {
            AustromApplication.knownUsers = dbProvider.getUsersByBudget(AustromApplication.appUser?.activeBudgetId!!)
        }
    }

    private fun setUpNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_drawerLayout_dly)) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            //v.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = insets.bottom }
            //toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            toolbar.setPadding(0, insets.top, 0,0)
            navigationHeaderLayout.setPadding(0, insets.top, 0,0)
            bottomNavigationBar.setPadding(0,0,0,insets.bottom)
            //v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
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