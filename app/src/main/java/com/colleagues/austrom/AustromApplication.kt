package com.colleagues.austrom

import android.app.Application
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.User

class AustromApplication() : Application() {
    var appUser : User? = null
    var activeAssets : MutableList<Asset> = mutableListOf()

}

