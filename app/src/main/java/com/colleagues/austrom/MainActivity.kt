package com.colleagues.austrom

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.colleagues.austrom.AustromApplication.Companion.appUser
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.InvitationNotificationDialogFragment
import com.colleagues.austrom.dialogs.bottomsheetdialogs.SuggestQuickAccessDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.fragments.AnalyticsNetWorthHistoryFragment
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.fragments.AnalyticsSplitByCategoryFragment
import com.colleagues.austrom.fragments.ImportFragment
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.fragments.PlanningFragment
import com.colleagues.austrom.fragments.SettingsFragment
import com.colleagues.austrom.fragments.SharedBudgetEmptyFragment
import com.colleagues.austrom.fragments.SharedBudgetFragment
import com.colleagues.austrom.fragments.SharedBudgetJoinFragment
import com.colleagues.austrom.managers.SyncManager
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    //region Binding
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var mainLayout : ConstraintLayout
    private lateinit var navigationHeaderLayout : ConstraintLayout
    private lateinit var navigationView : NavigationView
    private lateinit var navigationUserNameTextView : TextView
    private lateinit var navigationLogOutButton: ImageButton
    private lateinit var bottomNavigationBar: NavigationBarView
    private lateinit var fragmentHolder: FragmentContainerView
    private fun bindViews() {
        drawerLayout = findViewById(R.id.main_drawerLayout_dly)
        mainLayout = findViewById(R.id.main_mainLayout_cly)
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_drawerLayout_dly)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            //val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //drawerLayout.setPadding(0, insets.top, 0, 0)
            navigationHeaderLayout.setPadding(0, insets.top, 0,0)
            mainLayout.setPadding(0, insets.top, 0,0)
            bottomNavigationBar.setPadding(0,0,0,insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setUpOrientationLimitations() { setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setUpOrientationLimitations()
        setContentView(R.layout.activity_main)
        bindViews()
        adjustInsets()
        fillInDefaultUsers()
        fillInDefaultCategories()
        fillInDefaultCurrencies()
        suggestSettingUpQuickAccessCode()
        notifyAboutBudgetInvitation()
        synchronizeWithBudget()
        navigationUserNameTextView.text = appUser?.username!!.startWithUppercase()
        navigationView.setNavigationItemSelectedListener { pressedMenuItem -> handleSideNavigationPanelClick(pressedMenuItem) }
        bottomNavigationBar.setOnItemSelectedListener { item -> handleBottomNavigationBarClick(item) }
        navigationLogOutButton.setOnClickListener { logOut() }
        onBackPressedDispatcher.addCallback(this) {}
        if (supportFragmentManager.findFragmentById(R.id.main_fragmentHolder_frg)==null) switchFragment(BalanceFragment()) else switchFragment(SettingsFragment())
    }

    private fun fillInDefaultUsers() {
        AustromApplication.knownUsers = LocalDatabaseProvider(this).getAllUsers()
    }

    private fun handleSideNavigationPanelClick(pressedMenuItem: MenuItem): Boolean {
        when(pressedMenuItem.itemId) {
            R.id.nav_sharedBudget_mit -> {
                val provider: IRemoteDatabaseProvider = FirebaseDatabaseProvider(this)
                val activeBudgetId = appUser?.activeBudgetId
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
            R.id.navbar_budget_mit -> switchFragment(AnalyticsSplitByCategoryFragment())
            R.id.navbar_planning_mit -> switchFragment(PlanningFragment())
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
            is BalanceFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is AnalyticsSplitByCategoryFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is AnalyticsNetWorthHistoryFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is OpsFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is ImportFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is PlanningFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is SettingsFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is SharedBudgetFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is SharedBudgetJoinFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
            is SharedBudgetEmptyFragment -> fragment.setOnNavigationDrawerOpenCalled { drawerLayout.open() }
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.commit()
    }

    private fun logOut() {
        (application as AustromApplication).forgetRememberedUser()
        (application as AustromApplication).forgetRememberedPin()
        appUser = null
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

    private fun notifyAboutBudgetInvitation() {
        val remoteDBProvider= FirebaseDatabaseProvider(this)
        val invitingBudgetId = remoteDBProvider.getTopInvitingBudgetId(appUser!!)
        if (!intent.getBooleanExtra("newUser", false) && !invitingBudgetId.isNullOrEmpty()) {
            val budget = remoteDBProvider.getBudgetById(invitingBudgetId)
            if (budget!=null) {
                val dialog = InvitationNotificationDialogFragment()
                dialog.setOnDialogResultListener { isAccepted ->
                    if (isAccepted)  {
                        switchFragment(SharedBudgetJoinFragment(budget))
                    } else {
                        budget.recallInvitationToUser(appUser!!, LocalDatabaseProvider(this), remoteDBProvider)
                    }
                }
                dialog.show(supportFragmentManager, "Invitation Received Dialog")
            }
        }
    }

    private fun synchronizeWithBudget() { SyncManager(this, LocalDatabaseProvider(this), FirebaseDatabaseProvider(this)).sync() }

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
                currency.exchangeRate = 1.0
                dbProvider.writeCurrency(currency)
            }
        }
        AustromApplication.activeCurrencies = Currency.switchRatesToNewBaseCurrency(
            Currency.localizeCurrencyNames(dbProvider.getCurrencies(), this), appUser?.baseCurrencyCode)
    }
}