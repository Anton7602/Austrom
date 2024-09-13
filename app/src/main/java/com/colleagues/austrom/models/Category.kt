package com.colleagues.austrom.models

import com.colleagues.austrom.R

class Category(val name: String? = null,
               val type: String? = null,
               val imgReference: Int? = null) {

    companion object{
        var defaultCategories : List<Category> = listOf(
            Category("Food", "Mandatory", R.drawable.ic_category_food_temp),
            Category("Clothes", "Mandatory", R.drawable.ic_category_clothes_temp),
            Category("Health", "Mandatory", R.drawable.ic_category_health_temp),
            Category("Rent", "Mandatory", R.drawable.ic_category_rent_temp),
            Category("Transport", "Mandatory", R.drawable.ic_category_transport_temp),
            Category("Entertainment", "Optional", R.drawable.ic_category_entertainment_temp),
            Category("Sport", "Optional", R.drawable.ic_category_sport_temp),
            Category("Subscriptions", "Optional", R.drawable.ic_category_subscriptions_temp),
            Category("Presents", "Optional", R.drawable.ic_category_presents_temp),
            Category("Travel", "Optional", R.drawable.ic_category_travel_temp),
            Category("Education", "Optional", R.drawable.ic_category_education_temp),
            Category("Equipment", "Optional", R.drawable.ic_category_equipment_temp),
            Category("Other", "Optional", R.drawable.ic_category_other_temp),
        )
    }


}