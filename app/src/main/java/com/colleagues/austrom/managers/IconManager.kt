package com.colleagues.austrom.managers

import androidx.annotation.DrawableRes
import com.colleagues.austrom.R

class IconManager {
    fun getAllAvailableIcons(): List<Icon> {
        return Icon.entries
    }

    fun getIconByResourceId(resourceId: Int): Icon? {
        Icon.entries.forEach { icon -> if (icon.resourceId==resourceId) return icon }
        return null
    }
}

enum class Icon(@DrawableRes val resourceId: Int = R.drawable.ic_placeholder_icon) {
    I0(R.drawable.ic_placeholder_icon),
    I1(R.drawable.ic_category_beauty_temp),
    I2(R.drawable.ic_category_cashback_temp),
    I3(R.drawable.ic_category_clothes_temp),
    I4(R.drawable.ic_category_education_temp),
    I5(R.drawable.ic_category_entertainment_temp),
    I6(R.drawable.ic_category_equipment_temp),
    I7(R.drawable.ic_category_food_temp),
    I8(R.drawable.ic_category_health_temp),
    I9(R.drawable.ic_category_other_temp),
    I10(R.drawable.ic_category_presents_temp),
    I11(R.drawable.ic_category_rent_temp),
    I12(R.drawable.ic_category_sport_temp),
    I13(R.drawable.ic_category_subscriptions_temp),
    I14(R.drawable.ic_category_transfer_temp),
    I15(R.drawable.ic_category_transport_temp),
    I16(R.drawable.ic_category_travel_temp),
    I17(R.drawable.ic_category_unused1_temp),
    I18(R.drawable.ic_category_unused2_temp),
    I19(R.drawable.ic_category_unused3_temp),
    I20(R.drawable.ic_category_unused4_temp),
    I21(R.drawable.ic_category_unused5_temp),
    I22(R.drawable.ic_category_unused6_temp),
    I23(R.drawable.ic_category_unused7_temp),
    I24(R.drawable.ic_category_unused8_temp),
    I25(R.drawable.ic_category_unused9_temp),
    I26(R.drawable.ic_category_unused10_temp),
    I27(R.drawable.ic_category_unused11_temp),
    I28(R.drawable.ic_category_unused12_temp),
    I29(R.drawable.ic_category_unused13_temp),
    I30(R.drawable.ic_category_unused14_temp),
    I31(R.drawable.ic_category_unused15_temp),
    I32(R.drawable.ic_category_unused16_temp),
    I33(R.drawable.ic_category_unused17_temp),
    I34(R.drawable.ic_category_unused18_temp),
    I35(R.drawable.ic_category_unused19_temp),
    I36(R.drawable.ic_category_unused20_temp),
    I37(R.drawable.ic_category_unused21_temp),
    I38(R.drawable.ic_category_unused22_temp),
    I39(R.drawable.ic_category_unused23_temp),
    I40(R.drawable.ic_category_unused24_temp),
    I41(R.drawable.ic_category_unused25_temp),
    I42(R.drawable.ic_category_unused26_temp),
    I43(R.drawable.ic_category_unused27_temp),
    I44(R.drawable.ic_category_unused28_temp),
    I45(R.drawable.ic_category_unused29_temp),
    I46(R.drawable.ic_category_unused30_temp),
    I47(R.drawable.ic_category_unused31_temp),
    I48(R.drawable.ic_category_unused32_temp),
    I49(R.drawable.ic_category_unused33_temp),
    I50(R.drawable.ic_category_unused34_temp),
    I51(R.drawable.ic_category_unused35_temp),
    I52(R.drawable.ic_category_unused36_temp),
    I53(R.drawable.ic_category_unused37_temp),
    I54(R.drawable.ic_category_unused38_temp),
    I55(R.drawable.ic_category_unused39_temp),
    I56(R.drawable.ic_category_unused40_temp),
    I57(R.drawable.ic_category_unused41_temp),
    I58(R.drawable.ic_category_unused42_temp),
    I59(R.drawable.ic_category_unused43_temp),
    I60(R.drawable.ic_category_unused44_temp),
    I61(R.drawable.ic_category_unused45_temp),
    I62(R.drawable.ic_category_wages_temp),
    I63(R.drawable.ic_category_unused46_temp),
    I64(R.drawable.ic_navigation_add_temp),
}