package com.colleagues.austrom

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.Asset
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.textfield.TextInputEditText

class NewAssetActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextInputEditText
    private lateinit var amountTextView: TextInputEditText
    private lateinit var typeChipGroup: ChipGroup
    private lateinit var currencyChipGroup: ChipGroup
    private lateinit var createNewAssetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_asset)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        createNewAssetButton.setOnClickListener {
            val provider : IDatabaseProvider = FirebaseDatabaseProvider()
            val assetType : Chip = findViewById(typeChipGroup.checkedChipId)
            val currencyType : Chip = findViewById(currencyChipGroup.checkedChipId)
            provider.writeNewAsset(Asset(
                assetType_id = getTypeID(assetType.text.toString()),
                assetName = titleTextView.text.toString(),
                user_id = (this.application as AustromApplication).appUser?.userID.toString(),
                amount = amountTextView.text.toString().toDouble(),
                currency_id = getCurrencyID(currencyType.text.toString()),
                isPrivate = false
            ))
            this.finish()
        }
    }

    private fun bindViews() {
        titleTextView = findViewById(R.id.newass_title_txt)
        amountTextView = findViewById(R.id.newass_amount_txt)
        typeChipGroup = findViewById(R.id.newass_assetType_chpgrp)
        currencyChipGroup = findViewById(R.id.newass_currency_chpgrp)
        createNewAssetButton = findViewById(R.id.newass_createAsset_btn)
    }

    //REDO!!!!
    private fun getCurrencyID(currencyName : String) : Int {
        when(currencyName) {
            "Euro" -> return 0
            "Dollar" -> return 1
            "Rouble" -> return 2
        }
        return 0
    }

    //REDO!!!!
    private fun getTypeID(typeName : String) : Int {
        when(typeName) {
            "Card" -> return 0
            "Cash" -> return 1
            "Investment" -> return 2
        }
        return 0
    }
}