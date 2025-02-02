package com.colleagues.austrom

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.AssetFilterDialogFragment
import com.colleagues.austrom.dialogs.SuggestQuickAccessDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.fragments.BudgetFragment
import com.colleagues.austrom.fragments.ImportFragment
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.fragments.SettingsFragment
import com.colleagues.austrom.fragments.SharedBudgetEmptyFragment
import com.colleagues.austrom.fragments.SharedBudgetFragment
import com.colleagues.austrom.managers.SyncManager
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    //region Binding
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var toolbar : Toolbar
    private lateinit var filterButton: ImageButton
    private lateinit var navigationHeaderLayout : ConstraintLayout
    private lateinit var navigationView : NavigationView
    private lateinit var navigationUserNameTextView : TextView
    private lateinit var navigationLogOutButton: ImageButton
    private lateinit var bottomNavigationBar: NavigationBarView
    private lateinit var fragmentHolder: FragmentContainerView
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
    //endregion
    //region Localization
    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.attachBaseContext(newBase)
        } else  {
            super.attachBaseContext(AustromApplication.updateBaseContextLocale(newBase))
        }
    }
    //endregion
    //region Styling
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
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        bindViews()
        setSupportActionBar(toolbar)
        adjustInsets()
        fillInDefaultCategories()
        fillInDefaultCurrencies()
        if (AustromApplication.appUser?.activeBudgetId!=null) {
            val remoteDBProvider = FirebaseDatabaseProvider(this)
            val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
            if (currentBudget!=null) {
                SyncManager(this, LocalDatabaseProvider(this), FirebaseDatabaseProvider(this)).sync()
            }
        }
        suggestSettingUpQuickAccessCode()
        setUpNavigationDrawer()
        navigationUserNameTextView.text = AustromApplication.appUser?.username!!.startWithUppercase()
        filterButton.setOnClickListener { handleFilterButtonClick() }
        navigationView.setNavigationItemSelectedListener { pressedMenuItem -> handleSideNavigationPanelClick(pressedMenuItem) }
        bottomNavigationBar.setOnItemSelectedListener { item -> handleBottomNavigationBarClick(item) }
        navigationLogOutButton.setOnClickListener { logOut() }
        onBackPressedDispatcher.addCallback(this) {}
    }

    private fun handleFilterButtonClick() {
        when (fragmentHolder.getFragment<Fragment>()) {
            is BalanceFragment -> AssetFilterDialogFragment(fragmentHolder.getFragment()).show(supportFragmentManager, "Suggest Quick Access Code Dialog")
        }
    }

    private fun handleSideNavigationPanelClick(pressedMenuItem: MenuItem): Boolean {
        when(pressedMenuItem.itemId) {
            R.id.nav_sharedBudget_mit -> {
                val provider: IRemoteDatabaseProvider = FirebaseDatabaseProvider(this)
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
            R.id.nav_import_export_mit ->  switchFragment(ImportFragment())
            R.id.nav_settings_mit ->  switchFragment(SettingsFragment())
            else -> return false
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleBottomNavigationBarClick(pressedButton: MenuItem): Boolean {
        when(pressedButton.itemId) {
            R.id.navbar_balance_mit -> switchFragment(BalanceFragment())
            R.id.navbar_budget_mit -> switchFragment(BudgetFragment())
            R.id.navbar_ops_mit -> switchFragment(OpsFragment())
            else -> return false
        }
        return true
    }

    /**
     * Called when a view has been clicked.
     * @param fragment The view that was clicked.
     */
    fun switchFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragmentHolder_frg, fragment)
        when (fragment) {
            is BalanceFragment -> filterButton.visibility = View.VISIBLE
            is OpsFragment -> filterButton.visibility = View.GONE
            else -> filterButton.visibility = View.GONE
        }
        transaction.commit()
    }

    private fun logOut() {
        (application as AustromApplication).forgetRememberedUser()
        (application as AustromApplication).forgetRememberedPin()
        AustromApplication.appUser = null
        AustromApplication.activeAssets = mutableMapOf()
        AustromApplication.knownUsers = mutableMapOf()
        val dbProvider = LocalDatabaseProvider(this)
        dbProvider.deleteAllUsers()
        this.finish()
    }

    private fun suggestSettingUpQuickAccessCode() {
        if (intent.getBooleanExtra("newUser",false) && (application as AustromApplication).getRememberedPin()==null) {
            SuggestQuickAccessDialogFragment().show(supportFragmentManager, "Suggest Quick Access Code Dialog")
        }
    }

    private fun setUpNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun fillInDefaultCategories() {
        val dbProvider = LocalDatabaseProvider(this)
        if (dbProvider.getCategories().isEmpty()) {
            for (category in Category.localizeCategoriesNames(Category.defaultTransferCategories, this)) {
                dbProvider.writeCategory(category)
            }

            for (category in Category.localizeCategoriesNames(Category.defaultExpenseCategories, this)) {
                dbProvider.writeCategory(category)
            }

            for (category in Category.localizeCategoriesNames(Category.defaultIncomeCategories, this)) {
                dbProvider.writeCategory(category)
            }
        }
        val activeCategories = dbProvider.getCategories()
        activeCategories.forEach { category -> AustromApplication.activeCategories[category.categoryId] = category }
    }

    private fun fillInDefaultCurrencies() {
        val dbProvider = LocalDatabaseProvider(this)
        if (dbProvider.getCurrencies().isEmpty()) {
            val currencies = Currency.getSupportedCurrenciesList()
            var currenciesMap = mutableMapOf<String, Currency>()
            for (currency in currencies) {
                currenciesMap[currency.code] = currency
            }
            currenciesMap = Currency.localizeCurrencyNames(currenciesMap, this)
            for (item in currenciesMap) {
                val currency = item.value
                currency.exchangeRate = 0.0
                dbProvider.writeCurrency(currency)
            }
        }
        AustromApplication.activeCurrencies = Currency.switchRatesToNewBaseCurrency(
            Currency.localizeCurrencyNames(dbProvider.getCurrencies(), this), AustromApplication.appUser?.baseCurrencyCode)
    }
}